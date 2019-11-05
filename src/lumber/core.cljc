(ns lumber.core
  (:require
   [uix.dom.alpha :as uix.dom]
   [uix.core.alpha :as uix]
   [xframe.core.alpha :as xf :refer [<sub]]
   #?(:cljs [cljs-bean.core :as bean])

   [lumber.ui :as ui]
   [lumber.dots :as dots]

   [goog.events :as events]
   [goog.events.EventType :as EventType]
   ))

(defn percentage [total x] (/ (* 100.0 x) total))

(defn scroll-percentage []
  (percentage (- js/document.documentElement.scrollHeight
                 js/document.documentElement.clientHeight)
              js/document.documentElement.scrollTop))

(xf/reg-event-db
 :db/init
 (fn [_ _]
   {:partners
    ["Casper" "Resy" "Choosy" "Patagonia" "Workframe" "Village Studio"]
    :projects
    {:vimsical {:video "https://www.youtube.com/watch?v=OtvK24bG_IY&feature=youtu.be"
                :code  "https://github.com/vimsical/vimsical"}
     :juergen  {:code  "https://github.com/lumberdev/Juergen"}}
    :open false
    :scroll 0
    :embed ""
    }))

(xf/reg-event-db :set-scroll
                 (fn [db [_ value]]
                   (assoc-in db [:scroll] (scroll-percentage))))

(xf/reg-event-db :open
                 (fn [db [_ value]]
                   (-> db
                       (assoc-in [:embed] value)
                       (assoc-in [:open] true))))

(xf/reg-event-db :close
                 (fn [db [_ value]]
                   (assoc-in db [:open] false)))

(xf/reg-sub :db/scroll   (fn [] (:scroll   (xf/<- [::xf/db]))))
(xf/reg-sub :db/partners (fn [] (:partners (xf/<- [::xf/db]))))
(xf/reg-sub :db/open     (fn [] (:open     (xf/<- [::xf/db]))))
(xf/reg-sub :db/embed    (fn [] (:embed     (xf/<- [::xf/db]))))

(defn progress []
 (let [scroll (<sub [:db/scroll])]
   [:div.progress
    [:div.state {:style {:width (str scroll "%")}}]
    [:div.rail]]))

(defn nav []
  [:header.main
   ;; [:div.logo  [:img {:src "images/Logo.svg"}]]
   [:div.logo  [:span.t1 "Lumber"]]
   [:div.nav   [progress]]
   [:div.email [:div.a.u
                [:span.disk-cont [:div.disk]]
                [:a.a {:href "mailto:hello@lumberdev.nyc"} "Build Something with Us"]]]])


(defn video-size [screen-width ratio margin]
  (cond (> screen-width 1200)
        {:width 662 :height 414}
        :else {:width (- screen-width (* 2 margin))
               :height (/ (- screen-width (* 2 margin)) ratio)}))

(defn popup []
  (let [open?         (<sub [:db/open])
        src           (<sub [:db/embed])
        screen-height js/document.documentElement.clientHeight
        screen-width  js/document.documentElement.clientWidth
        ratio         (/ 662 414)
        size          (video-size screen-width ratio 20)]
    [:div.popup {:on-click #(xf/dispatch [:close])
                 :style {:display (if open? "grid" "none")
                         :height (str  "px")}}
     [:article
      [:div.btn-x {:on-click #(xf/dispatch [:close])}
       [:div.x.l]
       [:div.x.r]]
      [:iframe {:width (str (:width size))
                :height (str (:height size))
                :src src
                :frameborder "0" :allow "accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"}]]]))

(defn footer []
  [:footer.main.t1 [:div.left "Made in New York & Sofia"] [:div.center ""] [:div.right "All Rights Reserved"]])

(defn grid []
  [:div.grid
   [:section.box.info
    [:div.ar
     [:header  [:h3.h3 "Who we are"]]
     [:article [:h2.h2 "We are a development firm that works with a global network with developers."]]
     [:footer  [:div.btn.re.black "Email Us"]]]]

   [:section.box.value
    [:div.ar
     [:article.black-bg [:h2.h2.white "Full-Time and Part-Time Development"]]]]

   [:section.box.work.green-gr-h
    [:div.ar
     [:header.title  [:h3.h3 "Open Source"]]
     [:header.name   [:h3.h3 "Vimsical"]]
     [:article.desc  [:h2.h2 "Vimsical"] [:p.t1 "Version control for digital education"]]
     [:article.image [:img {:src "/images/Vimsical.jpg"}]]
     [:footer        [:a.a.u {:on-click #(xf/dispatch [:open "https://www.youtube.com/embed/OtvK24bG_IY"])}
                      [:div.video [:span.play] "Play Video"]]
      [:div.pipe]
      [:a.a.u.link {:href "https://github.com/vimsical/vimsical" :target "_blank"}
       [:div "See Source" [:span "\u2197"]]]]]]

   [:section.box.info.black-bg
    [:div.ar
     [:header  [:h3.h3 "Clients & Partners"]]
     [:article [:ul.white (for [p (<sub [:db/partners])] ^{:keys p} [:li.h2 [:a p]])]]
     [:footer  [:div.btn.re.white "Build Something With Us"]]]]

   [:section.box.info
    [:div.ar
     [:header  [:h3.h3 "Say Hello"]]
     [:article [:p.h2 "Send Us Electronic Mail"]]
     [:footer  [:div.btn.re.black "hello@lumberdev.nyc"]]]]

   [:section.box.work.peach-gr-h
    [:div.ar
     [:header  [:h3.h3 "Open Source"]]
     [:article [:h2.h2 "Zeal"] [:p.t1 "Clipboard manager meets programming environment"]]
     [:footer  [:a.a.u {:href "https://github.com/vimsical/vimsical" :target "_blank"} [:div.video "Learn More"]]
      [:div.pipe]
      [:a.a.u.link {:href "https://github.com/vimsical/vimsical" :target "_blank"}
       [:div "See Source" [:span "\u2197"]]]]]]

   [:section.box.work.purple-gr-h
    [:div.ar
     [:header  [:h3.h3 "AuthO-clojure"]]
     [:article [:h2.h2 "authO-clojure"] [:p.t1 "Clojure client library for the [Auth0] platform"]]
     [:footer  [:a.a.u {:href "" :target "_blank"} [:div.video ""]]
      [:a.btn.re.black {:href "https://github.com/lumberdev/auth0-clojure" :target "_blank"}
       [:div "See Source"]]]]]

   [:section.box.work.green-gr-h
    [:div.ar
     [:header  [:h3.h3 "In-House Project"]]
     [:article [:h2.h2 "Juergen"] [:p.t1 "A self-shooting camera app in React Native"]]
     [:footer  [:a.btn.re.black {:href "https://github.com/lumberdev/Juergen" :target "_blank"} "Go to site"]]]]

   [:section.box.work.purple-gr-h
    [:div.ar
     [:header.title  [:h3.h3 "In-House Project"]]
     [:header.name   [:h3.h3 "Duct"]]
     [:article.desc  [:h2.h2 "Duct"] [:p.t1 "IFTTT for developers"]]
     [:article.image [:img {:src "/images/Duct.jpg"}]]
     [:footer
      [:a.btn.re.black {:href "https://github.com/lumberdev/duct" :target "_blank"}
       [:div "Go to Site"]]]]]

   [:section.box.value
    [:div.ar
     [:article.black-bg [:h2.h2.white "Ongoing Maintenance"]]]]

   [:section.box.value
    [:div.ar
     [:article.black-bg [:h2.h2.white "Specialized Engineers"]]]]

   [:section.box.value
    [:div.ar
     [:article.black-bg [:h2.h2.white "Dedicated Teams"]]]]

   [:section.box.quote
    [:div.ar
     [:article [:h2.h2 "The Future of Work is Remote"]]
     [:svg
      (for [p (dots/rand-pos-seq 7 300 300)] ;; FIX: read from element width
        [:circle {:cx (:x p)  :cy (:y p)  :r 20 :fill "#ffcc08"}])]]]

   [:section.box.dots.white-bg
    [:div.ar
     [:div.dot-grid
      (for [p (range 0 25)]
        [:div.dot.black-bg])]]]

    ;; [:svg
    ;;  (for [p (flatten dot-matrix)]
    ;;    [:circle {:cx (:x p) :cy (:y p)  :r 30 :fill "#000"}])
    ;;  ]
    ;; ]
   ])

(defn home []
  [:div
   [:div.cont
    [nav]
    [grid]
    [footer]]
   [popup]]
  )

(defn start []
  (prn "start")

  (defonce init-db (xf/dispatch [:db/init]))

  (events/listen js/window EventType/SCROLL (fn [e] (xf/dispatch [:set-scroll])))
  ;; (js/window.addEventListener "scroll" (fn [e] (xf/dispatch [:set-scroll])))

  (uix.dom/hydrate
   [home]
   (.getElementById js/document "app")))

(defn ^:export init []
;; init is called ONCE when the page loads
;; this is called in the index.html and must be exported
;; so it is available even in :advanced release builds
  (start))

(defn stop []
;; stop is called before any code is reloaded
;; this is controlled by :before-load in the config
  (prn "stop"))

(defn reload [] (prn "reload"))
