export const RouterLink = {};
export const RouterModule = {};
export const Router = class {
  navigate = jest.fn();
  navigateByUrl = jest.fn();
};
export const ActivatedRoute = class {
  snapshot = { data: {}, paramMap: { get: jest.fn() } };
};
export const CanActivateFn = () => {};
