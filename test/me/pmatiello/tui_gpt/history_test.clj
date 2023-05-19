(ns me.pmatiello.tui-gpt.history-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [me.pmatiello.tui-gpt.history :as history]
            [me.pmatiello.tui-gpt.support :as support]))

(use-fixtures :each support/with-spec-instrumentation)

(def ^:private history
  [{:role "user" :content "msg1" :tokens 1}])

(def ^:private new-message
  {:role "assistant" :content "msg2" :tokens 2})

(deftest append-test
  (testing "appends a message to history"
    (is (s/valid? ::history/history (history/append history new-message)))
    (is (= [{:role "user" :content "msg1" :tokens 1}
            {:role "assistant" :content "msg2" :tokens 2}]
           (history/append history new-message)))))
