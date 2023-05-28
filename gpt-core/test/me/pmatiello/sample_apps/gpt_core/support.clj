(ns me.pmatiello.sample-apps.gpt-core.support
  (:require [clojure.spec.test.alpha :as stest]))

(defn with-spec-instrumentation [f]
  (stest/instrument)
  (f)
  (stest/unstrument))
