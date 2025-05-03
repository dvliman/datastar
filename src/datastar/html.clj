(ns datastar.html
  (:require
   [clojure.string :as str]
   [clojure.java.io :as io]
   [ring.util.response :as response]
   [selmer.parser :as selmer]))

(defn render [template-path data]
  (->
   (str "templates/" template-path)
   (selmer/render-file data)
   (response/response)
   (response/content-type "text/html")
   (response/charset "UTF-8")))

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
