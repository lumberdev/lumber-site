(ns lumber.ui)

;; Example:
;; (defn pop-up [{:keys [on-close]}]
;;   [:div {:style {:position :absolute
;;                  :bottom 64
;;                  :left 64
;;                  :width 320
;;                  :height 200
;;                  :display :flex
;;                  :justify-content :center
;;                  :align-items :center
;;                  :background "#d4e1ec"
;;                  :box-shadow "0 2px 16px rgba(0, 0, 0, 0.05)"}}
;;    [:button {:on-click on-close} "Close"]])

;; (defn recipe []
;;    (let [open?* (-> db :open?)]
;;     [:div
;;      [:div#popup-layer]
;;      [:button.btn.re {:on-click #(reset! open?* true)} "Open popup"]
;;      #?(:cljs
;;         (when @open?*
;;           (uix.dom/create-portal [pop-up {:on-close #(reset! open?* false)}]
;;                              (.querySelector js/document "#popup-layer"))))]))

;; Example:
;; (defn button [{:keys [on-click]} text]
;;   [:button.btn {:on-click on-click} text])
;; (defn app []
;;   (let [state* (uix/state 0)]
;;     [:<>
;;      [button {:on-click #(swap! state* dec)} "-"]
;;      [:span @state*]
;;      [button {:on-click #(swap! state* inc)} "+"]]))
;; [app]
