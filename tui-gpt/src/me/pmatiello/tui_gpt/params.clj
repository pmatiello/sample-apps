(ns me.pmatiello.tui-gpt.params)

(def api-key
  (System/getenv "OPENAI_API_KEY"))

(def max-tokens
  2048)
