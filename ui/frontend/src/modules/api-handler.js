/**
 * api handler
 * Set the default value to null in the component.
 */
export class APIHandler {
  static buildUrl(request) {
    return `${request.join('/')}`;
  }
}