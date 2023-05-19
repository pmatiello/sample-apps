(ns me.pmatiello.tui-gpt.support
  (:require [clojure.spec.test.alpha :as stest]))

(defn with-spec-instrumentation [f]
  (stest/instrument)
  (f)
  (stest/unstrument))
