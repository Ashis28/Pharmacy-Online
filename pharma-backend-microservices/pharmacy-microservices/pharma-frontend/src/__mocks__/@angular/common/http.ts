export const HttpClient = class {
  get  = jest.fn();
  post = jest.fn();
  put  = jest.fn();
  delete = jest.fn();
  patch  = jest.fn();
};
export const HttpHeaders = class {
  constructor(public headers: any = {}) {}
};
export const HttpParams = class {};
