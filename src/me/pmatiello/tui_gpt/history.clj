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

(defn ^:private with-acc-tokens
  [history {:keys [tokens] :as each}]
  (let [acc-tokens (-> history first :acc-tokens (or 0))
        each       (assoc each :acc-tokens (+ acc-tokens tokens))]
    (conj history each)))

(defn compress
  [history size]
  (->> history
       reverse
       (reduce with-acc-tokens nil)
       (drop-while #(-> % :acc-tokens (> size)))
       (mapv #(dissoc % :acc-tokens))))

(s/fdef compress
  :args (s/cat :history ::history :size integer?)
  :ret ::history)
