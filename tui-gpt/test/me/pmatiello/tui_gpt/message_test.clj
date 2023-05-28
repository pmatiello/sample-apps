(ns me.pmatiello.tui-gpt.message-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [me.pmatiello.tui-gpt.message :as message]
            [me.pmatiello.tui-gpt.support :as support]))

(use-fixtures :each support/with-spec-instrumentation)

(deftest new-test
  (testing "creates a new message map"
    (is (s/valid? ::message/message (message/new "user" "content" 10)))
    (is (= {:role "user" :content "content" :tokens 10}
           (message/new "user" "content" 10)))))
