(ns me.pmatiello.sample-apps.tui-gpt.app
  (:gen-class)
  (:require [clojure.string :as str]
            [me.pmatiello.sample-apps.gpt-core.history :as history]
            [me.pmatiello.sample-apps.gpt-core.message :as message]
            [me.pmatiello.sample-apps.gpt-core.openai-api :as openai-api]
            [me.pmatiello.sample-apps.gpt-core.params :as params]
            [me.pmatiello.tui.core :as tui]))

(def ^:private api-config
  (openai-api/config params/api-key))

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

(defn ^:private print-response [message]
  (tui/println {:style [:bold :fg-purple] :body "response>"})
  (tui/println (:content message)))

(defn -main []
  (print-intro)

  (when-not params/api-key
    (tui/println "Error:" "$OPENAI_API_KEY is not defined.")
    (System/exit 1))

  (loop [history []]
    (let [prompt (read-prompt)]

      (when-not (empty? prompt)
        (print-progress)

        (let [prompt-tokens      (openai-api/token-count prompt api-config)
              prompt-msg         (message/new "user" prompt prompt-tokens)
              remaining-tokens   (- params/max-tokens prompt-tokens)
              compressed-history (history/compress history remaining-tokens)
              messages           (history/append compressed-history prompt-msg)
              resp-msg           (openai-api/chat messages api-config)
              new-history        (history/append messages resp-msg)]

          (print-response resp-msg)
          (recur new-history))))))
