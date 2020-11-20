(ns sine.something)

;; /* https://github.com/luwes/sinuous/blob/master/packages/sinuous/observable/src/observable.js */

(def empty-arr #js [])
(def tracking #js [])
(def queue #js [])

(def data #js {:empty-vec []
               :tracking nil
               :queue nil})

;; const EMPTY_ARR = [];
;; let tracking;
;; let queue;

;;;;;;;;;;;;;;;;

(def root)
(def reset-update)
(def sample)
(def transaction)
(def observable)
(def computed)
(def subscribe)
(def unsubscribe)
(def unsubscribe-)

;;;;;;;;;;;;;;;;

;; /**
;;  * Returns true if there is an active observer.
;;  * @return {boolean}
;;  */

(defn is-listening []
  (boolean (.-tracking data)))

;; export function isListening() {
;;   return !!tracking;
;; }

;;;;;;;;;;;;;;;; 

;; /**
;;  * Creates a root and executes the passed function that can contain computations.
;;  * The executed function receives an `unsubscribe` argument which can be called to
;;  * unsubscribe all inner computations.
;;  *
;;  * @param  {Function} fn
;;  * @return {*}
;;  */

(defn root [f]
  (let [prev-tracking (.-tracking data)
        root-update (fn [] nil)]
    (set! (.-tracking data) root-update)
    (reset-update root-update)
    (let [result (f (fn []
                      (unsubscribe- root-update)
                      (set! (.-tracking data) nil)))]
      (set! (.-tracking data) prev-tracking)
      result)))

;; export function root(fn) {
;;   const prevTracking = tracking;
;;   const rootUpdate = () => {};
;;   tracking = rootUpdate;
;;   resetUpdate(rootUpdate);
;;   const result = fn(() => {
;;     _unsubscribe(rootUpdate);
;;     tracking = undefined;
;;   });
;;   tracking = prevTracking;
;;   return result;
;; }

;;;;;;;;;;;;;;;;

;; /**
;;  * Sample the current value of an observable but don't create a dependency on it.
;;  *
;;  * @example
;;  * computed(() => { if (foo()) bar(sample(bar) + 1); });
;;  *
;;  * @param  {Function} fn
;;  * @return {*}
;;  */

(defn sample [f]
  (let [prev-tracking (.-tracking data)]
    (set! (.-tracking data) nil)
    (let [x (f)]
      (set! (.-tracking data) prev-tracking)
      x)))

;; export function sample(fn) {
;;   const prevTracking = tracking;
;;   tracking = undefined;
;;   const value = fn();
;;   tracking = prevTracking;
;;   return value;
;; }

;;;;;;;;;;;;;

;; /**
;;  * Creates a transaction in which an observable can be set multiple times
;;  * but only trigger a computation once.
;;  * @param  {Function} fn
;;  * @return {*}
;;  */

(defn transaction [f]
  (let [prev-queue (.-queue data)]
    (set! (.-queue data) [])
    (let [result (f)
          q (.-queue data)]
      (set! (.-queue data) prev-queue)
      (doseq [x q]
        (if (not= (.-empty-vec data) (.-pending x))
          (let [pending (.-pending x)]
            (set! (.-pending x) (.-empty-vec data))
            (x pending))))
      result)))

;; export function transaction(fn) {
;;   let prevQueue = queue;
;;   queue = [];
;;   const result = fn();
;;   let q = queue;
;;   queue = prevQueue;
;;   q.forEach((data) => {
;;     if (data._pending !== EMPTY_ARR) {
;;       const pending = data._pending;
;;       data._pending = EMPTY_ARR;
;;       data(pending);
;;     }
;;   });
;;   return result;
;; }

;;;;;;;;;;;;;;

;; /**
;;  * Creates a new observable, returns a function which can be used to get
;;  * the observable's value by calling the function without any arguments
;;  * and set the value by passing one argument of any type.
;;  *
;;  * @param  {*} value - Initial value.
;;  * @return {Function}
;;  */

(defn observable [x]
  (let [x #js {:value x}
        f (fn f
            
;; function observable(value) {
;;   function data(nextValue) {
            
            ([]
             (if (and 
                  (.-tracking data) 
                  (get (.-observers f) (.-tracking data) nil))
               (do
                 (conj (.-observers f) (.-tracking data))
                 (.push (.-observables (.-tracking data)) f)))
             (.-value x))

;;     if (arguments.length === 0) {
;;       if (tracking && !data._observers.has(tracking)) {
;;         data._observers.add(tracking);
;;         tracking._observables.push(data);
;;       }
;;       return value;
;;     }
            
            ([next-x]
             (if (.-queue data)
               (do
                 (if (= (.-pending f) (.-empty-vec data))
                   (.push (.-queue data) f))
                 (set! (.-pending f) next-x)
                 next-x))

;;     if (queue) {
;;       if (data._pending === EMPTY_ARR) {
;;         queue.push(data);
;;       }
;;       data._pending = nextValue;
;;       return nextValue;
;;     }
             
             (set! (.-value x) next-x)

;;     value = nextValue;
             
             (let [cleared-update (.-tracking data)]
               (set! (.-tracking data) nil)

;;     // Clear `tracking` otherwise a computed triggered by a set
;;     // in another computed is seen as a child of that other computed.
;;     const clearedUpdate = tracking;
;;     tracking = undefined;

               (set! (.-run-observers f) (Set. (.-observers f)))
               (.forEach (.-run-observers f)
                         #(set! (.-fresh %) false))
               (.forEach (.-run-observers f)
                         #(if-not (.-fresh %) (%)))

;;     // Update can alter data._observers, make a copy before running.
;;     data._runObservers = new Set(data._observers);
;;     data._runObservers.forEach((observer) => (observer._fresh = false));
;;     data._runObservers.forEach((observer) => {
;;       if (!observer._fresh) observer();
;;     });

               (set! (.-tracking data) cleared-update)
               (.-value x))))]
               
;;     tracking = clearedUpdate;
;;     return value;
;;   }

             (set! (.-$o f) 1)
             (set! (.-observers f) (Set.))
             (set! (.-pending f) (.-empty-vec data))
             
;;   // Tiny indicator that this is an observable function.
;;   // Used in sinuous/h/src/property.js
;;   data.$o = 1;
;;   data._observers = new Set();
;;   // The 'not set' value must be unique, so `nullish` can be set in a transaction.
;;   data._pending = EMPTY_ARR;

             f
             
;;   return data;
;; }

             ))
             
;; /**
;;  * @namespace
;;  * @borrows observable as o
;;  */
;; export { observable, observable as o };

;;;;;;;;;;;;;;;;;

;; /**
;;  * Creates a new computation which runs when defined and automatically re-runs
;;  * when any of the used observable's values are set.
;;  *
;;  * @param {Function} observer
;;  * @param {*} value - Seed value.
;;  * @return {Function} Computation which can be used in other computations.
;;  */

(defn computed [observer x]
  (let [x #js {:value x}
        update (fn update []
                 (let [prev-tracking (.-tracking data)]
                   (if (.-tracking data)
                     (.push (.-children (.-tracking data)) update))
                   (unsubscribe- update)
                   (set! (.-fresh update) true)
                   (set! (.-tracking data) update)
                   (set! (.-value x) (observer (.-value x)))
                   (set! (.-tracking data) prev-tracking)
                   (.-value x)))]
    (set! (.-update observer) update)))

;; function computed(observer, value) {
;;   observer._update = update;

;;   // if (tracking == null) {
;;   //   console.warn("computations created without a root or parent will never be disposed");
;;   // }

;;   resetUpdate(update);
;;   update();

;;   function update() {
;;     const prevTracking = tracking;
;;     if (tracking) {
;;       tracking._children.push(update);
;;     }

;;     _unsubscribe(update);
;;     update._fresh = true;
;;     tracking = update;
;;     value = observer(value);

;;     tracking = prevTracking;
;;     return value;
;;   }

;;   // Tiny indicator that this is an observable function.
;;   // Used in sinuous/h/src/property.js
;;   data.$o = 1;

;;   function data() {
;;     if (update._fresh) {
;;       if (tracking) {
;;         // If being read from inside another computed, pass observables to it
;;         update._observables.forEach((o) => o());
;;       }
;;     } else {
;;       value = update();
;;     }
;;     return value;
;;   }

;;   return data;
;; }

;; /**
;;  * @namespace
;;  * @borrows computed as c
;;  */
;; export { computed, computed as c };

;; /**
;;  * Run the given function just before the enclosing computation updates
;;  * or is disposed.
;;  * @param  {Function} fn
;;  * @return {Function}
;;  */
;; export function cleanup(fn) {
;;   if (tracking) {
;;     tracking._cleanups.push(fn);
;;   }
;;   return fn;
;; }

;; /**
;;  * Subscribe to updates of an observable.
;;  * @param  {Function} observer
;;  * @return {Function}
;;  */
;; function subscribe(observer) {
;;   computed(observer);
;;   return () => _unsubscribe(observer._update);
;; }

;; /**
;;  * @namespace
;;  * @borrows subscribe as c
;;  */
;; export { subscribe, subscribe as s };

;; /**
;;  * Unsubscribe from an observer.
;;  * @param  {Function} observer
;;  */
;; export function unsubscribe(observer) {
;;   _unsubscribe(observer._update);
;; }

;; function _unsubscribe(update) {
;;   update._children.forEach(_unsubscribe);
;;   update._observables.forEach((o) => {
;;     o._observers.delete(update);
;;     if (o._runObservers) {
;;       o._runObservers.delete(update);
;;     }
;;   });
;;   update._cleanups.forEach((c) => c());
;;   resetUpdate(update);
;; }

;; function resetUpdate(update) {
;;   // Keep track of which observables trigger updates. Needed for unsubscribe.
;;   update._observables = [];
;;   update._children = [];
;;   update._cleanups = [];
;; }
