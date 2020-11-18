(ns sine.core
  (:require [observable :refer [observable
                                o
                                subscribe
                                computed
                                sample
                                on
                                root
                                transaction]]
            [sine.h :refer [h hs]]))

(defn normalize-arguments [node node-builder]
  (let [n-1 (second node)
        children-start-idx (if (map? n-1) 2 1)
        props (if (= children-start-idx 2) n-1 {})
        children (mapv node-builder (subvec node children-start-idx))]
    [props children]))

(defn create-node-builder [h]
  (letfn [(build-nodes
           [node & args]
           (cond
             (> (count args) 0)
             (let [fragment (js/DocumentFragment.)]
               (.append fragment (build-nodes node))
               (dotimes [n (count args)]
                 (.append fragment (build-nodes (nth args n)))))
             
             (vector? node)
             (let [n-0 (first node)]
               (if
                (or (fn? n-0) (keyword? n-0) (string? n-0))
                 (let [[props children] (normalize-arguments node build-nodes)]
                   (apply h (into [(if (fn? n-0) n-0 (name n-0)) props] children)))
                 (apply h (map build-nodes node))))
             
             :else node))]
    build-nodes))

(defn render [selector view]
  (.append (js/document.querySelector selector) view))

(def html (create-node-builder h))
(def svg (create-node-builder hs))

(defn log [& args]
  (apply (.-log js/console) args))
(defn ? [x]
  (log "cljs ?" x)
  x)

(defn counter [{:keys [initial]}]
  (let [c (o initial)]
    (html
     [:div "Count: " c " "
      [:button {:onclick #(c (inc (c)))} "Inc"]])))

(defn input [{:keys [initial placeholder]}]
  (let [x (o initial)]
    (subscribe #(js/console.log "haha" (x)))
    (html
     [[:div "Something"]
      [:input {:placeholder placeholder
               :type "text"
               :value x
               :oninput #(x (.. % -target -value))}]
      ["p" x]])))

(defn app []
  (? (html
      [[:div
        [:h1 "Welcome to the app!"]
        [counter {:initial 3}]]
       [input {:initial "fing" :placeholder "wut"}]])))

(log "what?" (app))
(render "#app" (app))


;; (defn app []
;;   (h "div" (h "h1" "Welcome to the app?")))
  
;; (.append (.querySelector js/document "#app") (app))
