(ns me.pmatiello.tui-gpt.app
  (:require [clojure.string :as str]
            [me.pmatiello.openai-api.api :as openai]
            [me.pmatiello.tui.core :as tui]))

(defn ^:private exit
  ([]
   (exit nil))
  ([err]
   (case err
     :api-key
     (do (tui/println "Error:" "$OPENAI_API_KEY is not defined.")
         (exit 1))

     :max-tokens
     (do (tui/println "Error:" "invalid $OPENAI_API_MAX_TOKENS value.")
         (exit 2))

     (System/exit 0))))

(def ^:private api-key
  (or (System/getenv "OPENAI_API_KEY") (exit :api-key)))

(def ^:private max-tokens
  (try (or (some-> "OPENAI_API_MAX_TOKENS" System/getenv Integer/parseInt) 2048)
       (catch NumberFormatException e (exit :max-tokens))))

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
       (reduce
         (fn [acc {:keys [tokens] :as each}]
           (let [sum-tokens (-> acc first :sum-tokens (or 0))
                 each       (assoc each :sum-tokens (+ sum-tokens tokens))]
             (conj acc each)))
         nil)
       (drop-while #(-> % :sum-tokens (> size)))
       (mapv #(dissoc % :sum-tokens))))

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
