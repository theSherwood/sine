import { api } from "./hyper";
import * as observable from "./observable";

Object.assign(api, observable);

const { h } = api;

export {
  observable,
  o,
  subscribe,
  computed,
  sample,
  on,
  root,
  transaction,
} from "./observable";
export { api, h };
