(ns sine.atom
  (:require [observable :refer [o
                                c
                                root
                                subscribe
                                unsubscribe
                                sample
                                transaction]]))

(deftype SAtom [observable meta validator ^:mutable watches]
  IEquiv
  (-equiv [this other] (identical? this other))

  IDeref
  (-deref [_] (observable))

  IReset
  (-reset! [this new-value]
    (observable new-value)
    (when-not (nil? validator)
      (assert (validator new-value) "Validator rejected reference state"))
    (let [old-value (sample observable)]
      (when-not (= old-value new-value)
        (observable new-value)
        (when-not (nil? watches)
          (-notify-watches this old-value new-value)))
      new-value))

  IFn
  (-invoke [_] (observable))
  (-invoke [this new-value] (reset! this new-value))

  ISwap
  (-swap! [this f]
    (reset! this (f (sample observable))))
  (-swap! [this f x]
    (reset! this (f (sample observable) x)))
  (-swap! [this f x y]
    (reset! this (f (sample observable) x y)))
  (-swap! [this f x y more]
    (reset! this (apply f (sample observable) x y more)))

  IWatchable
  (-notify-watches [this oldval newval]
    (reduce-kv (fn [_ key f]
                 (f key this oldval newval)
                 nil)
               nil watches))
  (-add-watch [_ key f]
    (set! watches (assoc watches key f)))
  (-remove-watch [_ key]
    (set! watches (dissoc watches key))))

(defn atom [x meta validator watches]
  (let [observable (o x)]
    (SAtom. observable meta validator watches)))

(defn computed [f meta validator watches seed]
  (let [new-atom (atom seed meta validator watches)]
    (subscribe #(reset! new-atom (f)))
    new-atom))

(defn on [obs f meta validator watches seed]
  (let [new-atom (atom seed meta validator watches)]
    (subscribe #(do
                  (doseq [o obs] (o))
                  (reset! new-atom (sample f))))
    new-atom))

(defn watch [obs f]
  (subscribe #(do
                (doseq [o obs] (o))
                (sample f))))

;; Define these so they will be accessible from this namespace
(def atom- o)
(def computed- c)
(def root root)
(def subscribe subscribe)
(def unsubscribe unsubscribe)
(def sample sample)
(def transaction transaction)