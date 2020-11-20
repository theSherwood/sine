(ns sine.builder)

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
                 (.append fragment (build-nodes (nth args n))))
               fragment)
             
             (vector? node)
             (let [n-0 (first node)]
               (if (or (fn? n-0) (keyword? n-0) (string? n-0))
                 (let [[props children] (normalize-arguments node build-nodes)]
                   (apply h (into [(if (fn? n-0) n-0 (name n-0)) props] children)))
                 (apply h (map build-nodes node))))
             
             :else node))]
    build-nodes))
