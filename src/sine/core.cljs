(ns sine.core
  (:require
   [sine.h :refer [h hs]]
   [sine.builder :refer [create-node-builder]]
   [sine.atom :refer [atom computed on watch]]
   [observable :refer [o
                       c
                       subscribe
                       unsubscribe
                       sample
                       root
                       transaction]]))

(defn render [selector view]
  (.append (js/document.querySelector selector) view))

(def html (create-node-builder h))
(def svg (create-node-builder hs))

;; Define these so they will be accessible from this namespace
(def atom atom)
(def computed computed)
(def on on)
(def watch watch)
(def atom- o)
(def computed- c)
(def root root)
(def subscribe subscribe)
(def unsubscribe unsubscribe)
(def sample sample)
(def transaction transaction)
