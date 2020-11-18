(ns sine.h
  (:require [utils :refer [assign access]]
            [observable :refer [subscribe o]]
            [clojure.string :refer [lower-case]]))

(defn log [& args]
  (apply (.-log js/console) args))
(defn ? [x]
  (log "cljs ?" x)
  x)

;; /* https://github.com/luwes/sinuous/tree/master/packages/sinuous/h/src */

;; /** @type {[]} Instead of `any[]` */
;; const EMPTY_ARR = [];

;; /**
;;  * Internal API.
;;  * Consumer must provide an observable at api.subscribe<T>(observer: () => T).
;;  *
;;  * @typedef {boolean} hSVG Determines if `h` will build HTML or SVG elements
;;  * @type {{
;;  * h:         import('./hyper.js').hTag
;;  * s:         hSVG
;;  * insert:    import('./insert.js').hInsert
;;  * property:  import('./property.js').hProperty
;;  * add:       import('./add.js').hAdd
;;  * rm:        import('./remove-nodes.js').hRemoveNodes
;;  * subscribe: (observer: () => *) => void
;;  * }}
;;  */
;; // @ts-ignore Object is populated in index.js
;; const api = {};

(def listeners #js {})

(defn event-proxy [e]
  ((access listeners (.-type e)) e))
;; /**
;;  * Proxy an event to hooked event handlers.
;;  * @this Node & { _listeners: { [name: string]: (ev: Event) => * } }
;;  * @type {(e: Event) => *}
;;  */
;; function eventProxy(e) {
;;   // eslint-disable-next-line fp/no-this
;;   return this._listeners[e.type](e);
;; }

(defn handle-event [el name x]
  (let [name (lower-case (subs name 2))]
    (if x
      (.addEventListener el name event-proxy)
      (.removeEventListener el name event-proxy))
    
    ;; (.log js/console "name" name (.toString x))
    (assign x listeners name)
    ;; (set! (.. listeners name) x))) ;; This setter isn't correct  
  ))
    
;; /**
;;  * @type {(el: Node, name: string, value: (ev: Event?) => *) => void}
;;  */
;; const handleEvent = (el, name, value) => {
;;   name = name.slice(2).toLowerCase();

;;   if (value) {
;;     el.addEventListener(name, eventProxy);
;;   } else {
;;     el.removeEventListener(name, eventProxy);
;;   }

;;   (el._listeners || (el._listeners = {}))[name] = value;
;; };

(defprotocol ISine
  (h-builder [this svg])
  (add [this parent x end-mark])
  (cast-node [this x])
  (frag [this x])
  (property [this el x name is-attr is-css])
  (insert [this el x end-mark current start-node])
  )

(defrecord Sine []
  ISine
  (h-builder
    [this svg]
    (fn h [& args]
      (let [data #js {:el nil :args args}
            item (fn item [arg]
                  ;;  (.log js/console "arg" arg (fn? arg) (.-el data))
                   (cond
                     (nil? arg) nil

                     (string? arg)
                     (if (not (nil? (.-el data)))
                       (add this (.-el data) arg)
                       (set! (.-el data)
                             (if svg
                               (.createElementNS js/document "http://www.w3.org/2000/svg" arg)
                               (.createElement js/document arg))))

                     (seq? arg)
                     (do
                       (if-not (.-el data)
                         (set! (.-el data) (.createFragment js/document)))
                       (doseq [arg* (.-args data)] (item arg*)))

                     (instance? js/Node arg)
                     (if (.-el data)
                       (add this (.-el data) arg nil)
                       (set! (.-el data) arg))

                     (map? arg)
                     (property this (.-el data) arg nil svg)

                     (fn? arg)
                     (do
                      ;;  (log "arg" arg "el" (.-el data))
                       (if (.-el data)
                         (insert this (.-el data) arg (add this (.-el data) ""))
                         (set! (.-el data) (apply arg (let [args-vec (vec (.-args data))]
                                                        (set! (.-args data)
                                                              (subvec args-vec 0 1))
                                                        (subvec args-vec 1)))))) 

;;     } else if (typeof arg === "function") {
;;       if (el) {
;;         // See note in add.js#frag() - This is a Text('') node
;;         const endMark = /** @type {Text} */ (api.add(el, ""));
;;         api.insert(el, arg, endMark);
;;       } else {
;;         // Support Components
;;         el = arg.apply(null, args.splice(1));
;;       }
;;     }

                     :else (add this (.-el data) (str arg))))]

        (doseq [arg* (.-args data)] (item arg*))
        (.-el data))))
;; /**
;;  * Sinuous `h` tag aka hyperscript.
;;  * @typedef {HTMLElement | SVGElement | DocumentFragment} DOM
;;  * @typedef {(tag: string? | [], props: object?, ...children: Node | *) => DOM} hTag
;;  * @type {hTag}
;;  */
;; const h = (...args) => {
;;   let el;
;;   const item = (/** @type {*} */ arg) => {
;;     // @ts-ignore Allow empty if
;;     // eslint-disable-next-line eqeqeq
;;     if (arg == null);
;;     else if (typeof arg === "string") {
;;       if (el) {
;;         api.add(el, arg);
;;       } else {
;;         el = api.s
;;           ? document.createElementNS("http://www.w3.org/2000/svg", arg)
;;           : document.createElement(arg);
;;       }
;;     } else if (Array.isArray(arg)) {
;;       // Support Fragments
;;       if (!el) el = document.createDocumentFragment();
;;       arg.forEach(item);
;;     } else if (arg instanceof Node) {
;;       if (el) {
;;         api.add(el, arg);
;;       } else {
;;         // Support updates
;;         el = arg;
;;       }
;;     } else if (typeof arg === "object") {
;;       // @ts-ignore 0 | 1 is a boolean but can't type cast; they don't overlap
;;       api.property(el, arg, null, api.s);
;;     } else if (typeof arg === "function") {
;;       if (el) {
;;         // See note in add.js#frag() - This is a Text('') node
;;         const endMark = /** @type {Text} */ (api.add(el, ""));
;;         api.insert(el, arg, endMark);
;;       } else {
;;         // Support Components
;;         el = arg.apply(null, args.splice(1));
;;       }
;;     } else {
;;       // eslint-disable-next-line no-implicit-coercion,prefer-template
;;       api.add(el, "" + arg);
;;     }
;;   };
;;   args.forEach(item);
;;   return el;
;; };

      (cast-node
       [this x]
       (cond
         (string? x) (.createTextNode js/document x)
         (not (instance? js/Node x)) (h [] x)
         :else x))

;; /** @type {(value: *) => Text | Node | DocumentFragment} */
;; const castNode = (value) => {
;;   if (typeof value === "string") {
;;     return document.createTextNode(value);
;;   }
;;   // Note that a DocumentFragment is an instance of Node
;;   if (!(value instanceof Node)) {
;;     // Passing an empty array creates a DocumentFragment
;;     // Note this means api.add is not purely a subcall of api.h; it can nest
;;     return api.h(EMPTY_ARR, value);
;;   }
;;   return value;
;; };

      (frag
       [this x]
   ;; x is a Node
       (let [child-nodes (.-childNodes  x)]
         (cond
           (or (nil? child-nodes) (not= 11 (.-nodeType x))) nil
           (< (.-length child-nodes) 2) (first child-nodes)
           :else {:start-mark (add this x "" (first child-nodes))})))

;; /**
;;  * @typedef {{ _startMark: Text }} Frag
;;  * @type {(value: Text | Node | DocumentFragment) => (Node | Frag)?}
;;  */
;; const frag = (value) => {
;;   const { childNodes } = value;
;;   if (!childNodes || value.nodeType !== 11) return;
;;   if (childNodes.length < 2) return childNodes[0];
;;   // For a fragment of 2 elements or more add a startMark. This is required for
;;   // multiple nested conditional computeds that return fragments.

;;   // It looks recursive here but the next call's fragOrNode is only Text('')
;;   return {
;;     _startMark: /** @type {Text} */ (api.add(value, "", childNodes[0])),
;;   };
;; };
  ;; 
      (add
       [this parent x end-mark]
      ;;  (log x end-mark (cast-node this x) (frag this x))
       (log "cljs" "parent" parent "x" x "end-mark" end-mark)
       (let [x (cast-node this x)
             frag-or-node (or (frag this x) x)]
         (.insertBefore parent x (if (and end-mark (.-parentNode end-mark)) end-mark))
         frag-or-node))

;; /**
;;  * Add a string or node before a reference node or at the end.
;;  * @typedef {Node | string | number} Value
;;  * @typedef {(parent: Node, value: Value | Value[], endMark: Node?) => Node | Frag} hAdd
;;  * @type {hAdd}
;;  */
;; const add = (parent, value, endMark) => {
;;   value = castNode(value);
;;   const fragOrNode = frag(value) || value;

;;   // If endMark is `null`, value will be added to the end of the list.
;;   parent.insertBefore(value, endMark && endMark.parentNode && endMark);
;;   return fragOrNode;
;; };

      (insert
       [this el x end-mark current start-node]
       (log "cljs current?" current)
       (let [el (or (and end-mark (.-parentNode end-mark)) el)
             start-node (or start-node (if (instance? js/Node current) current))]
         
         (cond
           (= x current)
           current
           
           (and (or (not current) (string? current))
                (or (string? x) (number? x)))
           (do
             (log 
              "cljs" 
              "if" (or (nil? current) (not (.-firstChild el))) 
              "current" current 
              "firstChild"(.-firstChild el))
             (if (or (nil? current) (not (.-firstChild el)))
               (if end-mark
                 (add this el (str x) end-mark)
                 (set! (.-textContent el) (str x)))
               (if end-mark
                 (set! (.-data (or (.-previousSibling end-mark) (.-lastChild el))) (str x))
                 (set! (.. el -firstChild -data) (str x))))
             (str x))
           
           (fn? x)
           (let [box #js {:val current}]
             (subscribe
              #(do
                 (log "cljs current" current)
                 (set! (.-val box)
                       (insert this
                               el
                               (.call x #js {:el el :end-mark end-mark})
                               end-mark
                               (.-val box)
                               start-node))))
            ;;  (.log js/console (.-val box) end-mark)
             (.-val box))
           
;;   } else if (typeof value === "function") {
;;     api.subscribe(() => {
;;       current = api.insert(
;;         el,
;;         value.call({ el, endMark }),
;;         endMark,
;;         current,
;;         startNode
;;       );
;;     });
;;   }
           
           :else
           (do
             (if end-mark
               (if current
                 (let [start-node (or start-node
                                      (or (and (get current :start-mark nil)
                                               (.-nextSibling (get current :start-mark nil)))
                                          (.-previousSibling end-mark)))]
                   (remove-nodes this el start-node end-mark)))
               (set! (.-textContent el) ""))
             (if (and value (not= value true))
               (add this el x end-mark)
               nil)))))
      
;; /**
;;  * @typedef {import('./add.js').Frag} Frag
;;  * @typedef {(el: Node, value: *, endMark: Node?, current: (Node | Frag)?,
;;  * startNode: Node?) => Node | Frag } hInsert
;;  * @type {hInsert}
;;  */
;; const insert = (el, value, endMark, current, startNode) => {
;;   // This is needed if the el is a DocumentFragment initially.
;;   el = (endMark && endMark.parentNode) || el;

;;   // Save startNode of current. In clear() endMark.previousSibling is not always
;;   // accurate if content gets pulled before clearing.
;;   startNode = startNode || (current instanceof Node && current);

;;   // @ts-ignore Allow empty if statement
;;   if (value === current);
;;   else if (
;;     (!current || typeof current === "string") &&
;;     // @ts-ignore Doesn't like `value += ''`
;;     // eslint-disable-next-line no-implicit-coercion
;;     (typeof value === "string" || (typeof value === "number" && (value += "")))
;;   ) {
;;     // Block optimized for string insertion.
;;     // eslint-disable-next-line eqeqeq
;;     if (current == null || !el.firstChild) {
;;       if (endMark) {
;;         api.add(el, value, endMark);
;;       } else {
;;         // Using textContent is a lot faster than append -> createTextNode.
;;         el.textContent = /** @type {string} See `value += '' */ (value);
;;       }
;;     } else {
;;       if (endMark) {
;;         (endMark.previousSibling || el.lastChild).data = value;
;;       } else {
;;         el.firstChild.data = value;
;;       }
;;     }
;;     current = value;
;;   } else if (typeof value === "function") {
;;     api.subscribe(() => {
;;       current = api.insert(
;;         el,
;;         value.call({ el, endMark }),
;;         endMark,
;;         current,
;;         startNode
;;       );
;;     });
;;   } else {
;;     // Block for nodes, fragments, Arrays, non-stringables and node -> stringable.
;;     if (endMark) {
;;       // `current` can't be `0`, it's coerced to a string in insert.
;;       if (current) {
;;         if (!startNode) {
;;           // Support fragments
;;           startNode =
;;             (current._startMark && current._startMark.nextSibling) ||
;;             endMark.previousSibling;
;;         }
;;         api.rm(el, startNode, endMark);
;;       }
;;     } else {
;;       el.textContent = "";
;;     }
;;     current = null;

;;     if (value && value !== true) {
;;       current = api.add(el, value, endMark);
;;     }
;;   }

;;   return current;
;; };

      (property
       [this el x name* is-attr is-css]
      ;;  (.log js/console "el" el "x" x (map? x) "name" (str name*) "is-attr" is-attr "is-css" is-css)
       (cond
         (nil? x) nil
         
         (or (nil? name*) (and (= name* "attrs") is-attr))
         (doseq [[n v] x]
          ;;  (.log js/console "n" n (str n) "v" v)
          ;;  (.log js/console "n" (name n))
           (property this el v (name n) is-attr is-css)) ;; deal with keywords
         
         (and (= (first name*) "o") (= (second name*) "n") (not (.-$o x)))
         (handle-event el name* x)
         
         (fn? x)
         (subscribe #(property this el (.call x #js {:el el :name name*}) name* is-attr is-css))
         
         is-css
         ((.. el -style -setProperty) name* x)
         
         (or is-attr (= (subs name* 0 5) "data-") (= (subs name* 0 5) "aria-"))
         (.setAttribute el name* x)
         
         (= name* "style")
         (if (string? x)
           (set! (.. el -style -cssText) x)
           (property this el x nil is-attr true))
         
         (= name* "class")
         (assign x el "className")
         
         :else
         (assign x el name*)))
      
;; /**
;;  * @typedef {(el: Node, value: *, name: string, isAttr: boolean?, isCss: boolean?) => void} hProperty
;;  * @type {hProperty}
;;  */
;; const property = (el, value, name, isAttr, isCss) => {
;;   // eslint-disable-next-line eqeqeq
;;   if (value == null) return;
;;   if (!name || (name === "attrs" && (isAttr = true))) {
;;     for (name in value) {
;;       api.property(el, value[name], name, isAttr, isCss);
;;     }
;;   } else if (name[0] === "o" && name[1] === "n" && !value.$o) {
;;     // Functions added as event handlers are not executed
;;     // on render unless they have an observable indicator.
;;     handleEvent(el, name, value);
;;   } else if (typeof value === "function") {
;;     api.subscribe(() => {
;;       api.property(el, value.call({ el, name }), name, isAttr, isCss);
;;     });
;;   } else if (isCss) {
;;     el.style.setProperty(name, value);
;;   } else if (
;;     isAttr ||
;;     name.slice(0, 5) === "data-" ||
;;     name.slice(0, 5) === "aria-"
;;   ) {
;;     el.setAttribute(name, value);
;;   } else if (name === "style") {
;;     if (typeof value === "string") {
;;       el.style.cssText = value;
;;     } else {
;;       api.property(el, value, null, isAttr, true);
;;     }
;;   } else {
;;     if (name === "class") name += "Name";
;;     el[name] = value;
;;   }
;; };

      (remove-nodes
       [this parent start-node end-mark]
       (loop [start-node start-node]
         (let [ n (.-nextSibling start-node)]
           (if (= parent (.-parentNode start-node))
             (.removeChild parent start-node))
           (recur n))))
      
;; /**
;;  * Removes nodes, starting from `startNode` (inclusive) to `endMark` (exclusive).
;;  * @typedef {(parent: Node, startNode: Node, endMark: Node) => void} hRemoveNodes
;;  * @type {hRemoveNodes}
;;  */
;; const removeNodes = (parent, startNode, endMark) => {
;;   while (startNode && startNode !== endMark) {
;;     const n = startNode.nextSibling;
;;     // Is needed in case the child was pulled out the parent before clearing.
;;     if (parent === startNode.parentNode) {
;;       parent.removeChild(startNode);
;;     }
;;     startNode = n;
;;   }
;; };

;; api.insert = insert;
;; api.property = property;
;; api.add = add;
;; api.rm = removeNodes;
;; api.h = h;

;; export { api };
      )
(def sine (Sine.))
(def h (h-builder sine false))
(def hs (h-builder sine true))

;; (.log js/console h hs)

(def blarney 8)
(def foo (o 10))
;; (subscribe #(log (foo)))
(def view (h "div"
             (h "div" foo)
             (h "button" {:onClick #(foo (inc (foo)))} "thing")))

;; (.log js/console view)

(defn render [selector view]
  (.append (js/document.querySelector selector) view))

(render "#app" view)
