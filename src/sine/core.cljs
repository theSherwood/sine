(ns sine.core
  (:require
   [sine.h :refer [h hs]]
   [sine.builder :refer [create-node-builder]]
   [sine.atom :as sa]))

(defn render [selector view]
  (.append (js/document.querySelector selector) view))

(def html (create-node-builder h))
(def svg (create-node-builder hs))

;; Define these so they will be accessible from this namespace
(def atom sa/atom)
(def computed sa/computed)
(def on sa/on)
(def watch sa/watch)
(def atom- sa/atom-)
(def computed- sa/computed-)
(def root sa/root)
(def subscribe sa/subscribe)
(def unsubscribe sa/unsubscribe)
(def sample sa/sample)
(def transaction sa/transaction)
