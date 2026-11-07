// Netlify Serverless Function: SaferPath API Gateway
// Handles /api/locations, /api/routes, /api/navigation/*, /api/safety/*, /api/network-info

const locations = [
  { key: "erode_center", lat: 11.3410, lng: 77.7172, address: "Erode City Center" },
  { key: "erode_railway", lat: 11.3460, lng: 77.7225, address: "Erode Railway Station" },
  { key: "erode_bus", lat: 11.3385, lng: 77.7280, address: "Erode Bus Stand" },
  { key: "perundurai", lat: 11.2756, lng: 77.5878, address: "Perundurai" },
  { key: "sathy", lat: 11.5053, lng: 77.2384, address: "Sathyamangalam" },
  { key: "gobi", lat: 11.4550, lng: 77.4392, address: "Gobichettipalayam" },
  { key: "bhavani", lat: 11.4471, lng: 77.6846, address: "Bhavani" },
  { key: "turmeric_market", lat: 11.3450, lng: 77.7200, address: "Turmeric Market" },
  { key: "textile_market", lat: 11.3430, lng: 77.7150, address: "Textile Market" },
  { key: "medical_college", lat: 11.2800, lng: 77.6000, address: "Government Medical College" },
  { key: "cbe_road", lat: 11.3300, lng: 77.7000, address: "Coimbatore Road" },
  { key: "salem_road", lat: 11.3500, lng: 77.7300, address: "Salem Road" },
  { key: "trichy_road", lat: 11.3200, lng: 77.7400, address: "Trichy Road" }
];

const locationMap = new Map(locations.map(loc => [loc.key, loc]));

function getDistance(lat1, lon1, lat2, lon2) {
  const R = 6371;
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return Math.round(R * c * 10) / 10;
}

function generateWaypoints(start, end) {
  const waypoints = [];
  const numWaypoints = 3;
  const types = ["Main Junction", "Illuminated Avenue", "Police Checkpoint", "Transit Hub"];
  
  for (let i = 1; i <= numWaypoints; i++) {
    const progress = i / (numWaypoints + 1);
    const lat = start.lat + (end.lat - start.lat) * progress;
    const lng = start.lng + (end.lng - start.lng) * progress;
    waypoints.push({
      lat: Math.round(lat * 10000) / 10000,
      lng: Math.round(lng * 10000) / 10000,
      address: `${types[i % types.length]} (${Math.round(progress * 100)}% of journey)`
    });
  }
  return waypoints;
}

function getSafetyTier(score) {
  if (score >= 80) return { level: "VERY_SAFE", emoji: "🟢", name: "Very Safe", color: "#27ae60" };
  if (score >= 60) return { level: "SAFE", emoji: "🟡", name: "Safe", color: "#3498db" };
  if (score >= 40) return { level: "MODERATE", emoji: "🟠", name: "Moderate", color: "#f39c12" };
  return { level: "UNSAFE", emoji: "🔴", name: "Unsafe", color: "#e74c3c" };
}

// In-memory navigation session store for serverless execution
const activeSessions = new Map();

exports.handler = async (event, context) => {
  const path = event.path.replace(/^\/\.netlify\/functions\/api/, '').replace(/^\/api/, '');
  const method = event.httpMethod;
  const query = event.queryStringParameters || {};

  const headers = {
    'Content-Type': 'application/json',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS'
  };

  if (method === 'OPTIONS') {
    return { statusCode: 204, headers, body: '' };
  }

  try {
    // 1. GET /locations
    if (path === '/locations' || path === '/locations/') {
      return {
        statusCode: 200,
        headers,
        body: JSON.stringify(locations)
      };
    }

    // 2. GET /network-info
    if (path === '/network-info' || path === '/network-info/') {
      return {
        statusCode: 200,
        headers,
        body: JSON.stringify({
          localIP: "Netlify Cloud Edge (Global CDN)",
          hostname: event.headers.host || "saferpath.netlify.app",
          serverPort: 443,
          status: "ONLINE",
          region: "Global Edge Network"
        })
      };
    }

    // 3. GET /routes?from=...&to=...
    if (path === '/routes' || path === '/routes/') {
      const fromKey = query.from;
      const toKey = query.to;

      if (!fromKey || !toKey) {
        return {
          statusCode: 400,
          headers,
          body: JSON.stringify({ error: "Missing 'from' or 'to' parameters" })
        };
      }

      const start = locationMap.get(fromKey) || { lat: 11.3410, lng: 77.7172, address: fromKey };
      const end = locationMap.get(toKey) || { lat: 11.3460, lng: 77.7225, address: toKey };

      const baseDistance = Math.max(1.2, getDistance(start.lat, start.lng, end.lat, end.lng));
      const baseDuration = Math.max(5, Math.round(baseDistance * 2.8));

      const routes = [
        {
          routeId: `route_${fromKey}_${toKey}_safe`,
          name: `Arterial Safe Corridor (${start.address.split(' ')[0]} to ${end.address.split(' ')[0]})`,
          startLocation: start,
          endLocation: end,
          duration: baseDuration,
          formattedDuration: `${baseDuration} min`,
          distance: baseDistance,
          formattedDistance: `${baseDistance.toFixed(1)} km`,
          safetyScore: 92,
          safetyLevel: "Very Safe",
          safetyEmoji: "🟢",
          safetyFactors: [
            "Continuous LED Street Lighting",
            "High Pedestrian Density & Active Shops",
            "Frequent Police Mobile Patrols",
            "Integrated City CCTV Surveillance"
          ],
          waypoints: generateWaypoints(start, end)
        },
        {
          routeId: `route_${fromKey}_${toKey}_direct`,
          name: `Direct Commercial Highway Route`,
          startLocation: start,
          endLocation: end,
          duration: Math.max(4, Math.round(baseDuration * 0.85)),
          formattedDuration: `${Math.max(4, Math.round(baseDuration * 0.85))} min`,
          distance: Math.max(1.0, Math.round((baseDistance * 0.9) * 10) / 10),
          formattedDistance: `${Math.max(1.0, Math.round((baseDistance * 0.9) * 10) / 10).toFixed(1)} km`,
          safetyScore: 78,
          safetyLevel: "Safe",
          safetyEmoji: "🟡",
          safetyFactors: [
            "Moderate Street Lighting",
            "Commercial Traffic Hub",
            "Nearest Emergency Hospital: 1.5 km"
          ],
          waypoints: generateWaypoints(start, end)
        }
      ];

      return {
        statusCode: 200,
        headers,
        body: JSON.stringify(routes)
      };
    }

    // 4. POST /navigation/start
    if (path === '/navigation/start' || path === '/navigation/start/') {
      const body = JSON.parse(event.body || '{}');
      const sessionId = 'nav_' + Date.now() + '_' + Math.random().toString(36).substr(2, 6);
      
      const session = {
        sessionId,
        userId: body.userId || 'guest_user',
        routeId: body.routeId,
        isActive: true,
        startTime: new Date().toISOString(),
        progressPercent: 10,
        speed: 38.5,
        remainingDistance: 3.2,
        remainingTime: 7,
        currentInstruction: "Head northeast on Main Arterial Road towards Erode Central",
        nextVoiceInstruction: "In 250 meters, continue straight through the illuminated junction",
        currentLocation: { lat: 11.3415, lng: 77.7180 },
        routePath: [
          { lat: 11.3410, lng: 77.7172, address: "Origin Point" },
          { lat: 11.3425, lng: 77.7190, address: "Way Point 1" },
          { lat: 11.3440, lng: 77.7210, address: "Way Point 2" },
          { lat: 11.3460, lng: 77.7225, address: "Destination Hub" }
        ],
        safetyAnalysis: {
          safetyEmoji: "🟢",
          safetyDisplayName: "Very Safe",
          safetyReports: [
            { type: "SURVEILLANCE", description: "City Police Patrol Active", emoji: "🛡️" }
          ]
        }
      };

      activeSessions.set(sessionId, session);

      return {
        statusCode: 200,
        headers,
        body: JSON.stringify({
          sessionId,
          status: "navigation_started",
          message: "Navigation started successfully",
          session
        })
      };
    }

    // 5. GET /navigation/progress
    if (path === '/navigation/progress' || path === '/navigation/progress/') {
      const sessionId = query.sessionId;
      let session = activeSessions.get(sessionId);

      if (!session) {
        session = {
          sessionId: sessionId || 'default_nav',
          isActive: true,
          speed: 35.0 + Math.random() * 5,
          remainingDistance: Math.max(0.5, 2.8 - Math.random() * 0.4),
          remainingTime: Math.max(1, 5),
          currentInstruction: "Continue along illuminated corridor with active streetlights",
          nextVoiceInstruction: "Destination approaching in 400 meters on the right",
          currentLocation: { lat: 11.3435 + (Math.random() * 0.002), lng: 77.7205 + (Math.random() * 0.002) },
          routePath: [
            { lat: 11.3410, lng: 77.7172, address: "Origin Point" },
            { lat: 11.3425, lng: 77.7190, address: "Safe Waypoint 1" },
            { lat: 11.3440, lng: 77.7210, address: "Safe Waypoint 2" },
            { lat: 11.3460, lng: 77.7225, address: "Destination Hub" }
          ],
          safetyAnalysis: {
            safetyEmoji: "🟢",
            safetyDisplayName: "Very Safe",
            safetyReports: [
              { type: "PATROL", description: "Police escort vehicle active on route", emoji: "🚓" }
            ]
          }
        };
      }

      return {
        statusCode: 200,
        headers,
        body: JSON.stringify(session)
      };
    }

    // 6. POST /navigation/stop
    if (path === '/navigation/stop' || path === '/navigation/stop/') {
      const body = JSON.parse(event.body || '{}');
      if (body.sessionId) {
        activeSessions.delete(body.sessionId);
      }
      return {
        statusCode: 200,
        headers,
        body: JSON.stringify({ success: true, message: "Navigation stopped successfully" })
      };
    }

    // 7. GET /safety/analysis
    if (path === '/safety/analysis' || path === '/safety/analysis/') {
      const routeId = query.routeId || 'sample';
      const analysis = {
        routeId,
        safetyEmoji: "🛡️",
        safetyDisplayName: "Very Safe Route",
        baseScore: 90,
        adjustedScore: 94,
        colorCode: "#27ae60",
        recommendations: [
          "Optimal route for women commuters and night-time travel.",
          "Over 95% continuous LED street lighting throughout the segment.",
          "2 Police assistance booths and 3 emergency SOS kiosks along this corridor.",
          "High foot traffic and operational commercial establishments until 11:30 PM."
        ],
        safetyReports: [
          {
            type: "TRAFFIC NOTICE",
            description: "Minor road widening work on shoulder - main road clear.",
            severity: 15,
            safetyLevel: "Safe",
            emoji: "⚠️"
          }
        ],
        alternativeRoutes: [
          {
            routeId: "alt_bypass_route",
            name: "Outer Ring Illuminated Bypass",
            formattedDistance: "4.8 km",
            formattedDuration: "14 min",
            safetyEmoji: "🟢",
            safetyLevel: "Very Safe",
            safetyScore: 91
          }
        ]
      };

      return {
        statusCode: 200,
        headers,
        body: JSON.stringify(analysis)
      };
    }

    // Default 404
    return {
      statusCode: 404,
      headers,
      body: JSON.stringify({ error: `Endpoint ${path} not found` })
    };

  } catch (err) {
    return {
      statusCode: 500,
      headers,
      body: JSON.stringify({ error: "Internal Server Error", message: err.message })
    };
  }
};
