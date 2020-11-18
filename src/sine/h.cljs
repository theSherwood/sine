(ns sine.h
  (:require [utils :refer [assign access]]
            [observable :refer [subscribe o]]
            [clojure.string :refer [lower-case]]))

;; A port of https://github.com/luwes/sinuous/tree/master/packages/sinuous/h

(def listeners #js {})

(defn event-proxy [e]
  ((access listeners (.-type e)) e))

(defn handle-event [el name x]
  (let [name (lower-case (subs name 2))]
    (if x
      (.addEventListener el name event-proxy)
      (.removeEventListener el name event-proxy))
    
    (assign x listeners name)
  ))

(def h)
(def hs)

(def h-builder)
(def add)
(def cast-node)
(def frag)
(def property)
(def insert)
(def remove-nodes)

(defn h-builder [svg]
  (fn h [& args]
    (let [data #js {:el nil :args args}
          item (fn item [arg]
                 (cond
                   (nil? arg) nil

                   (string? arg)
                   (if (not (nil? (.-el data)))
                     (add (.-el data) arg nil)
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
                     (add (.-el data) arg nil)
                     (set! (.-el data) arg))

                   (map? arg)
                   (property (.-el data) arg nil svg nil)

                   (fn? arg)
                   (if (.-el data)
                     (insert (.-el data) arg (add (.-el data) "" nil) nil nil)
                     (set! (.-el data) (apply arg (let [args-vec (vec (.-args data))]
                                                    (set! (.-args data)
                                                          (subvec args-vec 0 1))
                                                    (subvec args-vec 1)))))

                   :else (add (.-el data) (str arg) nil)))]

      (doseq [arg* (.-args data)] (item arg*))
      (.-el data))))

(defn cast-node [x]
  (cond
    (string? x) (.createTextNode js/document x)
    (not (instance? js/Node x)) (h [] x)
    :else x))

(defn frag [x]
  (let [child-nodes (.-childNodes  x)]
    (cond
      (or (nil? child-nodes) (not= 11 (.-nodeType x))) nil
      (< (.-length child-nodes) 2) (first child-nodes)
      :else {:start-mark (add x "" (first child-nodes))})))

(defn add [parent x end-mark]
  (let [x (cast-node x)
        frag-or-node (or (frag x) x)]
    (.insertBefore parent x (if (and end-mark (.-parentNode end-mark)) end-mark))
    frag-or-node))

(defn insert [el x end-mark current start-node]
  (let [el (or (and end-mark (.-parentNode end-mark)) el)
        start-node (or start-node (if (instance? js/Node current) current))]

    (cond
      (= x current)
      current

      (and (or (not current) (string? current))
           (or (string? x) (number? x)))
      (do
        (if (or (nil? current) (not (.-firstChild el)))
          (if end-mark
            (add el (str x) end-mark)
            (set! (.-textContent el) (str x)))
          (if end-mark
            (set! (.-data (or (.-previousSibling end-mark) (.-lastChild el))) (str x))
            (set! (.. el -firstChild -data) (str x))))
        (str x))

      (fn? x)
      (let [box #js {:val current}]
        (subscribe
         #(set! (.-val box)
                (insert el
                        (.call x #js {:el el :end-mark end-mark})
                        end-mark
                        (.-val box)
                        start-node)))
        (.-val box))

      :else
      (do
        (if end-mark
          (if current
            (let [start-node (or start-node
                                 (or (and (get current :start-mark nil)
                                          (.-nextSibling (get current :start-mark nil)))
                                     (.-previousSibling end-mark)))]
              (remove-nodes el start-node end-mark)))
          (set! (.-textContent el) ""))
        (if (and x (not= x true))
          (add el x end-mark)
          nil)))))

(defn property [el x name* is-attr is-css]
  (cond
    (nil? x) nil

    (or (nil? name*) (and (= name* "attrs") is-attr))
    (doseq [[n v] x]
      (property el v (name n) is-attr is-css)) ;; deal with keywords

    (and (= (first name*) "o") (= (second name*) "n") (not (.-$o x)))
    (handle-event el name* x)

    (fn? x)
    (subscribe #(property el (.call x #js {:el el :name name*}) name* is-attr is-css))

    is-css
    ((.. el -style -setProperty) name* x)

    (or is-attr (= (subs name* 0 5) "data-") (= (subs name* 0 5) "aria-"))
    (.setAttribute el name* x)

    (= name* "style")
    (if (string? x)
      (set! (.. el -style -cssText) x)
      (property el x nil is-attr true))

    (= name* "class")
    (assign x el "className")

    :else
    (assign x el name*)))

(defn remove-nodes [parent start-node end-mark]
  (loop [start-node start-node]
    (if-not (= start-node end-mark)
      (let [n (.-nextSibling start-node)]
        (if (= parent (.-parentNode start-node))
          (.removeChild parent start-node))
        (recur n)))))

(def h (h-builder false))
(def hs (h-builder true))
