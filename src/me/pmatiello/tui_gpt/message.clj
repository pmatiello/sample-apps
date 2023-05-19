(ns me.pmatiello.tui-gpt.message)

(defn new
  [role content tokens]
  {:role role :content content :tokens tokens})
