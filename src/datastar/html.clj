(ns datastar.html
  (:require
   [charred.api :as charred]
   [clojure.string :as str]
   [clojure.java.io :as io]
   [ring.util.response :as response]
   [selmer.parser :as selmer]))

(def ^:private bufSize 1024)
(def read-json (charred/parse-json-fn {:async? false :bufsize bufSize}))

(defn render
  ([template-path]
   (render template-path {}))
  ([template-path data]
   (->
    (str "templates/" template-path)
    (selmer/render-file data)
    (response/response)
    (response/content-type "text/html")
    (response/charset "UTF-8"))))

(defn fragment
  ([template-path]
   (fragment template-path {}))
  ([template-path data]
   (:body (render template-path data))))

(defn template-exists?
  [template-path]
  (io/resource (str "templates/" template-path)))

(defn template-handler []
  (fn [request]
    (let [path (-> (:uri request)
                   (str/replace #"^/" "")
                   (str/replace #"/$" ""))

          template-name (if (str/blank? path) "index" path)
          template-path (if (str/ends-with? template-name ".html")
                          template-name
                          (str template-name ".html"))

          context {:request request :uri (:uri request)}]

      (if (template-exists? template-path)
        (render template-path context)
        (response/not-found "<h1>Page Not Found</h1>")))))
