import { api } from "./hyper";
import * as observable from "./observable";

Object.assign(api, observable);

const { h } = api;

console.warn("js");

let foo = observable.o(10);
let view = h(
  "div",
  h("div", foo),
  h("button", { onClick: () => foo(foo() + 1) }, "thing")
);

document.querySelector("#app").append(view);

// (def blarney 8)
// (def foo (o 10))
// (subscribe #(log (foo)))
// (def view (h "div"
//              (h "p" "something" 7 "else")
//              (h "p" blarney)
//              (h "div" foo)
//              (h "button" {:onClick #(foo (inc (foo)))} "thing")))

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
