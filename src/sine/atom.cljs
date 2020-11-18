(ns sine.atom)

(def empty-array #js [])
(def tracking)
(def queue)

(defn listening? []
  true? tracking)

(defn reset-update [update]
  (set! (.-observables update) #js [])
  (set! (.-children update) #js [])
  (set! (.-cleanups update) #js []))


; function resetUpdate(update) {
;   // Keep track of which observables trigger updates. Needed for unsubscribe.
;   update._observables = [];
;   update._children = [];
;   update._cleanups = [];
; }

(defn -unsubscribe [update]
  (dotimes [n (count (.-children update))]
    (-unsubscribe (nth (.-children update) n)))
  (dotimes [n (count (.-observables update))]
    (let [o (nth (.-observables update) n)]
      (disj (.-observers o) update)
      (if (not (zero? (count (.-run-observers o))))
        (disj (.-run-observers o) update)
        ())))
  (dotimes [n (count (.-cleanups update))]
    ((nth (.-cleanups update) n)))
  (reset-update update))

; function _unsubscribe(update) {
;   update._children.forEach(_unsubscribe);
;   update._observables.forEach(o => {
;     o._observers.delete(update);
;     if (o._runObservers) {
;       o._runObservers.delete(update);
;     }
;   });
;   update._cleanups.forEach(c => c());
;   resetUpdate(update);
; }

(defn unsubscribe [observer]
  (-unsubscribe (.-update observer)))

; export function unsubscribe(observer) {
;   _unsubscribe(observer._update);
; }

(defn cleanup [callback]
  (if tracking (.push (.-cleanups tracking) callback) ())
  callback)

; export function cleanup(fn) {
;   if (tracking) {
;     tracking._cleanups.push(fn);
;   }
;   return fn;
; }

(defn root [callback]
  (let [prev-tracking tracking
        root-update #()]
    (set! tracking root-update)
    (reset-update root-update)
    (let [result (callback #(do (-unsubscribe (root-update))
                                (set! tracking)))]
      (set! tracking prev-tracking)
      result)))

; export function root(fn) {
;   const prevTracking = tracking;
;   const rootUpdate = () => {};
;   tracking = rootUpdate;
;   resetUpdate(rootUpdate);
;   const result = fn(() => {
;     _unsubscribe(rootUpdate);
;     tracking = undefined;
;   });
;   tracking = prevTracking;
;   return result;
; }

(defn sample [callback]
  (let [prev-tracking tracking]
    (set! tracking)
    (let [result (callback)]
      (set! tracking prev-tracking)
      result)))

; export function sample(fn) {
;   const prevTracking = tracking;
;   tracking = undefined;
;   const value = fn();
;   tracking = prevTracking;
;   return value;
; }

(defn remove-fresh-children [update]
  (if (.-fresh update)
    (dotimes [n (count (.-observables update))]
      (let [o (nth (.-observables update) n)]
        (if (not (zero? (count (.-run-observers o))))
          (disj (.-run-observers o) update)
          ())))
    ()))

; function removeFreshChildren(u) {
;   if (u._fresh) {
;     u._observables.forEach(o => {
;       if (o._runObservers) {
;         o._runObservers.delete(u);
;       }
;     });
;   }
; }

;; (defn observable [value]
;;   (letfn [(data [next-value]
;;             (if (zero? (count (js/arguments)))
;;               (do (if (and tracking (not (contains? (.-observers data) tracking)))
;;                     (do (conj (.-observers data) tracking)
;;                         (.push (.-observables tracking) data))
;;                     ())
;;                   value)
;;               ((if queue
;;                  (do (if (= (.-pending data) empty-array)
;;                        (.push queue data)
;;                        ())
;;                      (set! (.-pending data) next-value)
;;                      next-value)
;;                  (do (set! value next-value)
;;                      (let [cleared-update tracking]
;;                        (set! tracking)
;;                        (set! (.-run-observers data) #{(.-observers data)})
;;                        (dotimes [n (count (.-run-observers data))]
;;                          (let [observer (nth (.-run-observers data) n)]
;;                            (set! (.-fresh observer) true)))
;;                        (dotimes [n (count (.-run-observers data))]
;;                          (let [observer (nth (.-run-observers data) n)]
;;                            (if (not (.-fresh observer)) (observer) ())))
;;                        (set! tracking cleared-update)
;;                        value))))))]
;;     (set! (.-$o data) true)
;;     (set! (.-observers data) #{})
;;     (set! (.-pending data) empty-array)
;;     data))

; function observable(value) {
;   function data(nextValue) {
;     if (arguments.length === 0) {
;       if (tracking && !data._observers.has(tracking)) {
;         data._observers.add(tracking);
;         tracking._observables.push(data);
;       }
;       return value;
;     }

;     if (queue) {
;       if (data._pending === EMPTY_ARR) {
;         queue.push(data);
;       }
;       data._pending = nextValue;
;       return nextValue;
;     }

;     value = nextValue;

;     // Clear `tracking` otherwise a computed triggered by a set
;     // in another computed is seen as a child of that other computed.
;     const clearedUpdate = tracking;
;     tracking = undefined;

;     // Update can alter data._observers, make a copy before running.
;     data._runObservers = new Set(data._observers);
;     data._runObservers.forEach(observer => (observer._fresh = false));
;     data._runObservers.forEach(observer => {
;       if (!observer._fresh) observer();
;     });

;     tracking = clearedUpdate;
;     return value;
;   }

;   // Tiny indicator that this is an observable function.
;   data.$o = true;
;   data._observers = new Set();
;   // The 'not set' value must be unique, so `nullish` can be set in a transaction.
;   data._pending = EMPTY_ARR;

;   return data;
; }


; (set! tracking 1)
; (js/console.log tracking)
; (set! tracking 100000)
; (js/console.log tracking)
; (set! tracking)
; (js/console.log tracking)

; (def some-set #{100})
; (js/console.log (count some-set))

; (def arr #js [1 2 3 4])
; (js/console.log (count arr))

; (js/console.log (if (not (zero? 0)) 1 10000000))

; (def -something 10)
; (js/console.log -something)

; (def arr #js [1 2 3 4])
; (.push arr 100)
; (js/console.log arr "huh")

; (and true (js/console.log 10000))