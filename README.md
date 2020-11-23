# sine

A port of [Sinuous](https://github.com/luwes/sinuous) to [ClojureScript](http://github.com/clojure/clojurescript)

[![Clojars Project](http://clojars.org/sine/latest-version.svg)](http://clojars.org/sine)

*WORK IN PROGRESS*

## Rationale

Rather than wrapping [React](http://facebook.github.io/react/) as [Reagent](https://github.com/reagent-project/reagent) does, sine is a port of a smaller, faster, simpler alternative: [Sinuous](https://github.com/luwes/sinuous).

[Sinuous](https://github.com/luwes/sinuous), and by extension sine, lean far more heavily into the reactive paradigm than [React](http://facebook.github.io/react/). Reactive updates are fine-grained and at the sub-component level.

## Examples

If you have any experience with [Reagent](https://github.com/reagent-project/reagent), most of this should look moderately familiar.

```clj
(defn counter [{:keys [initial]}]
  (let [c (s/atom (or initial 0))]
    (html
     [:div "Count: " c " "
      [:button {:onclick #(swap! c inc)} "Inc"]])))
```

## Acknowledgements

Big shout out to the incomparable [Wesley Luyten](https://github.com/luwes) for [Sinuous](https://github.com/luwes/sinuous), without which, sine would not exist
