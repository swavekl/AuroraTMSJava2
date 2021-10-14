/**
 * Helper class for constructing google maps urls
 */
export class NavigateUtil {

  public static getNavigationURL(streetAddress: string, city: string, state: string, venueName: string): string {
    let destination = null;
    if (venueName !== '') {
      destination = venueName;
      destination += ' ' + streetAddress;
    } else {
      destination = streetAddress;
    }
    destination += ' ' + city;
    destination += ' ' + state;
    destination = encodeURIComponent(destination);
    return `https://www.google.com/maps/dir/?api=1&travelmode=driving&dir_action=navigate&destination=${destination}`;
  }
}
