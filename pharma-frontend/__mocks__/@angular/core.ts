// Minimal Angular core stubs for Jest unit tests
// Replaces the full Angular runtime so tests run without a browser

export const Injectable = () => (target: any) => target;
export const Component  = () => (target: any) => target;
export const Input      = () => (target: any, key: string) => {};
export const NgModule   = () => (target: any) => target;
export const Pipe       = () => (target: any) => target;

// Signals — simple reactive wrappers
export function signal<T>(initial: T) {
  let value = initial;
  const sig: any = () => value;
  sig.set = (v: T) => { value = v; };
  sig.update = (fn: (v: T) => T) => { value = fn(value); };
  sig.asReadonly = () => sig;
  return sig;
}

export function computed<T>(fn: () => T) {
  return fn; // just return the function; calling it gives the value
}

// inject() — returns undefined; tests override dependencies manually
export function inject(token: any): any {
  return undefined;
}

export const OnInit = () => {};
export const OnDestroy = () => {};
export const ChangeDetectionStrategy = { OnPush: 'OnPush', Default: 'Default' };
export const ViewEncapsulation = { None: 0, Emulated: 2, ShadowDom: 3 };
