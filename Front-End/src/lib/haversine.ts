// ============================================================
// Haversine distance calculation
// Used for client-side nearby provider filtering.
//
// Backend currently has lat/lng on providers but NO dedicated
// /api/v1/providers/nearby endpoint. This module handles the
// MVP client-side approach.
//
// Architecture note: The API abstraction in src/api/provider.ts
// is designed so that when the backend adds:
//   GET /api/v1/providers/nearby?lat=&lng=&radiusKm=&page=&size=
// the data source can be swapped without changing UI components.
// ============================================================

const EARTH_RADIUS_KM = 6371

function toRadians(degrees: number): number {
  return degrees * (Math.PI / 180)
}

/**
 * Calculate great-circle distance between two coordinates.
 * Returns distance in kilometers.
 */
export function haversineDistance(
  lat1: number,
  lng1: number,
  lat2: number,
  lng2: number,
): number {
  const dLat = toRadians(lat2 - lat1)
  const dLng = toRadians(lng2 - lng1)

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRadians(lat1)) *
      Math.cos(toRadians(lat2)) *
      Math.sin(dLng / 2) *
      Math.sin(dLng / 2)

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return EARTH_RADIUS_KM * c
}

export interface WithCoordinates {
  latitude?: number | null
  longitude?: number | null
}

/**
 * Filter providers within a given radius of user location.
 * Attaches distanceKm to each provider.
 */
export function filterByRadius<T extends WithCoordinates>(
  items: T[],
  userLat: number,
  userLng: number,
  radiusKm: number,
): (T & { distanceKm: number })[] {
  return items
    .filter((item) => item.latitude != null && item.longitude != null)
    .map((item) => ({
      ...item,
      distanceKm: haversineDistance(userLat, userLng, item.latitude!, item.longitude!),
    }))
    .filter((item) => item.distanceKm <= radiusKm)
    .sort((a, b) => a.distanceKm - b.distanceKm)
}

/**
 * Format distance for display.
 * < 1km: show meters. >= 1km: show km with 1 decimal.
 */
export function formatDistance(km: number): string {
  if (km < 0.05) {
    return 'Nearby (< 50m)'
  }
  if (km < 1) {
    return `${Math.round(km * 1000)}m away`
  }
  return `${km.toFixed(1)} km away`
}
