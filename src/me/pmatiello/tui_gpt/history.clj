(ns me.pmatiello.tui-gpt.history)

(defn append
  [history message]
  (conj history message))

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
