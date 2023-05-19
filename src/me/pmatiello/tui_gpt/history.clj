(ns me.pmatiello.tui-gpt.history
  (:require [clojure.spec.alpha :as s]
            [me.pmatiello.tui-gpt.message :as message]))

(s/def ::history
  (and vector?
       (s/coll-of ::message/message)))

(defn append
  [history message]
  (conj history message))

(s/fdef append
  :args (s/cat :history ::history :message ::message/message)
  :ret ::history)

(defn compress
  [history size]
  (->> history
       reverse
       (reduce
         (fn [acc {:keys [tokens] :as each}]
           (let [sum-tokens (-> acc first :sum-tokens (or 0))
                 each       (assoc each :sum-tokens (+ sum-tokens tokens))]
             (conj acc each)))
         nil)
       (drop-while #(-> % :sum-tokens (> size)))
       (mapv #(dissoc % :sum-tokens))))
