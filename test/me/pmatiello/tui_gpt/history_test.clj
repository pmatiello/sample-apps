(ns me.pmatiello.tui-gpt.history-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [me.pmatiello.tui-gpt.history :as history]
            [me.pmatiello.tui-gpt.support :as support]))

(use-fixtures :each support/with-spec-instrumentation)

(def ^:private min-history
  [{:role "user" :content "msg1" :tokens 1}])

(def ^:private history
  [{:role "user" :content "msg1" :tokens 5}
   {:role "user" :content "msg2" :tokens 5}
   {:role "user" :content "msg3" :tokens 5}])

(def ^:private new-message
  {:role "assistant" :content "msg2" :tokens 2})

(deftest append-test
  (testing "appends a message to history"
    (is (s/valid? ::history/history (history/append min-history new-message)))
    (is (= [{:role "user" :content "msg1" :tokens 1}
            {:role "assistant" :content "msg2" :tokens 2}]
           (history/append min-history new-message)))))

(deftest compress-test
  (testing "compresses history up to the given number of tokens"
    (is (= [{:role "user" :content "msg2" :tokens 5}
            {:role "user" :content "msg3" :tokens 5}]
           (history/compress history 10))))

  (testing "compresses empty history to empty history"
    (is (empty? (history/compress [] 10))))

  (testing "compresses history to empty history if size is zero"
    (is (empty? (history/compress history 0))))

  (testing "compresses history to empty history if size is negative"
    (is (empty? (history/compress history -1)))))
