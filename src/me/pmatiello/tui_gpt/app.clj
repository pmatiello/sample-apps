(ns me.pmatiello.tui-gpt.app
  (:require [clojure.string :as str]
            [me.pmatiello.openai-api.api :as openai]
            [me.pmatiello.tui.core :as tui]))

(def ^:private api-key
  (or (System/getenv "OPENAI_API_KEY")
      (throw (ex-info "Error: $OPENAI_API_KEY is not defined." {}))))

(def ^:private config
  (openai/config :api-key api-key))

(defn ^:private print-intro []
  (tui/println {:style [:bold :fg-blue] :body "tui-gpt"})
  (tui/println "Enter prompts as multiple lines")
  (tui/println "Hit CTRL+D on an empty line to terminate a prompt."))

(defn ^:private read-prompt []
  (tui/println {:style [:bold :fg-purple] :body "prompt>"})
  (let [lines (tui/read-lines)]
    (str/join "\n" lines)))

(defn ^:private print-progress []
  (tui/println {:style [:fg-blue] :body "..."}))

(defn ^:private token-count [prompt]
  (-> {:model "text-embedding-ada-002" :input prompt}
      (openai/embedding config)
      :usage :prompt-tokens))

(defn ^:private new-message [role content tokens]
  {:role role :content content :tokens tokens})

(defn ^:private compress-history [history size]
  (->> history
       reverse
       vec
       (reduce
         (fn [acc each]
           (conj acc
                 (assoc each
                   :acc-tokens (-> acc last :acc-tokens (or 0) (+ (:tokens each))))))
         [])
       (take-while #(-> % :acc-tokens (<= size)))
       (map #(dissoc % :acc-tokens))
       reverse
       vec))

(def ^:private max-tokens
  2048)

(defn ^:private chat [messages]
  (let [messages (map #(select-keys % [:role :content]) messages)
        api-resp (openai/chat {:model "gpt-3.5-turbo" :messages messages} config)
        resp-msg (-> api-resp :choices first :message)
        tokens   (-> api-resp :usage :completion-tokens)]
    (new-message (:role resp-msg) (:content resp-msg) tokens)))

(defn ^:private print-response [message]
  (tui/println {:style [:bold :fg-purple] :body "response>"})
  (tui/println (:content message)))

(defn -main []
  (print-intro)

  (loop [history []]
    (let [prompt (read-prompt)]

      (when-not (empty? prompt)
        (print-progress)

        (let [prompt-tokens      (token-count prompt)
              prompt-msg         (new-message "user" prompt prompt-tokens)
              compressed-history (compress-history history (- max-tokens prompt-tokens))
              messages           (conj compressed-history prompt-msg)
              resp-msg           (chat messages)
              new-history        (conj messages resp-msg)]

          (print-response resp-msg)
          (recur new-history))))))
