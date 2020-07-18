(ns sine.core
  (:require [api :refer [h o]]))

(defn normalize-arguments [node node-builder]
  (let [n-1 (second node)
        children-start-idx (if (map? n-1) 2 1)
        props (clj->js (if (= children-start-idx 2) n-1 {}))
        children (clj->js (mapv node-builder (subvec node children-start-idx)))]
    [props children]))

; function normalize_arguments(node, node_builder) {
;   // Clarify which elements of the node array are
;   // children and which is the prop object
;   let children_start_idx =
;     node[1] && is_type(node[1], 'object') && !Array.isArray(node[1]) ? 2 : 1;
;   let props = children_start_idx == 2 ? node[1] : {};
;   let children = node
;     .slice(children_start_idx)
;     .map((child) => node_builder(child));
;   return [props, children];
; }

(defn create-node-builder [h]
  (letfn [(build-nodes
           [node & args]
           (cond
             (> (count args) 0)
             (let [fragment (js/DocumentFragment.)]
               (js/console.log "here" args)
               (.append fragment (build-nodes node))
               (dotimes [n (count args)]
                 (.append fragment (build-nodes (nth args n)))))
             (vector? node)
             (let [n-0 (first node)]
               (if
                (or (fn? n-0) (keyword? n-0) (string? n-0))
                 (let [[props children] (normalize-arguments node build-nodes)]
                ;;  (js/console.log "here2" args h (clj->js n-0) (clj->js node) children)
                   (h (if (fn? n-0) n-0 (name n-0)) props (into children)))
                 (apply h (mapv #(build-nodes %) node))))
             :else node))]
    build-nodes))

; function create_node_builder(h) {
;   return function build_nodes(node) {
;     if (arguments.length > 1) {
;       // Handle multiple top-level elements
;       let fragment = new DocumentFragment();
;       Array.from(arguments).forEach((content) =>
;         fragment.append(build_nodes(content))
;       );
;       return fragment;
;     }
;     if (Array.isArray(node)) {
;       if (is_type(node[0], 'function')) {
;         // Handle component
;         let [props, children] = normalize_arguments(node, build_nodes);
;         return h(node[0], props, ...children);
;       }
;       if (is_type(node[0], 'string')) {
;         // Handle css identifier shorthand
;         let [props, children] = normalize_arguments(node, build_nodes);
;         let [tagAndId, ...classes] = node[0].split('.');
;         let [tag, id] = tagAndId.split('#');
;         id && (props.id = id);
;         classes.length && (props.class = classes.join(' '));
;         return h(tag, props, ...children);
;       }

;       return h(...node.map((content) => build_nodes(content)));
;     }

;     return node;
;   };
; }
; 

(defn render [selector view]
  (.append (js/document.querySelector selector) view))

(def html (create-node-builder h))

(defn counter []
  (let [c (o 0)]
    (html
     [:div "Count: " c " "
      [:button {:onclick #(c (inc (c)))} "Inc"]])))

(defn app []
  (html
   [:div
    [:h1 "Welcome to the app!"]
    [counter]]))

;; (render "#app" (counter))
(render "#app" (app))

