(ns sine.atom
  (:require [observable :refer [o
                                subscribe
                                computed
                                sample
                                root
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
      (if-not (= old-value new-value)
        (do
          (observable new-value)
          (when-not (nil? watches)
            (-notify-watches this old-value new-value))))
      new-value))

  IFn
  (-invoke [this] (observable))
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
  (-add-watch [this key f]
    (set! watches (assoc watches key f)))
  (-remove-watch [this key]
    (set! watches (dissoc watches key))))

(defn atom [x meta validator watches]
  (let [observable (o x)]
    (SAtom. observable meta validator watches)))

;; rename
(defn computed- [f meta validator watches seed]
  (let [new-atom (atom seed meta validator watches)]
    (subscribe #(reset! new-atom (f)))
    new-atom))

;; rename
(defn on- [obs f meta validator watches seed]
  (let [new-atom (atom seed meta validator watches)]
    (subscribe #(do
                  (doseq [o obs] (o))
                  (reset! new-atom (sample f))))
    new-atom))

;; rename
(defn watch- [obs f meta validator watches]
  (subscribe #(do
                (doseq [o obs] (o))
                (sample f))))
