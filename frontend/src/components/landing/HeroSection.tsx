import { Button } from "@/components/ui/button";
import { ArrowRight, Play } from "lucide-react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";

interface HeroSectionProps {
  onDemo?: () => void;
}

export default function HeroSection({ onDemo }: HeroSectionProps) {
  const { t } = useTranslation("common");
  const navigate = useNavigate();
  return (
    <section className="relative min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-slate-50 pt-16 flex items-center">
      <div className="container mx-auto px-4 py-24">
        <div className="text-center max-w-4xl mx-auto">
          {/* Main Heading */}
          <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold text-gray-900 mb-6 leading-tight whitespace-pre-line">
            {t("hero.title")}
          </h1>
          
          {/* Description */}
          <p className="text-lg md:text-xl text-gray-600 mb-8 max-w-3xl mx-auto leading-relaxed whitespace-pre-line">
            {t("hero.description")}
          </p>
          
          {/* CTA Buttons */}
          <div className="flex flex-col sm:flex-row gap-4 justify-center mb-16">
            <Button 
              size="lg" 
              className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white px-8 py-3 text-lg font-medium"
              onClick={() => navigate("/register")}
            >
              {t("hero.freeStart")} <ArrowRight className="ml-2 h-5 w-5" />
            </Button>
            <Button 
              size="lg" 
              variant="outline" 
              className="border-gray-300 text-gray-700 hover:bg-gray-50 px-8 py-3 text-lg font-medium"
              onClick={onDemo}
            >
              <Play className="mr-2 h-5 w-5" /> {t("hero.watchDemo")}
            </Button>
          </div>
          
          {/* Kanban Board Preview */}
          <div className="max-w-4xl mx-auto">
            <div className="bg-white rounded-xl border border-gray-200 p-6 shadow-lg">
              <div className="bg-gradient-to-r from-blue-600 to-purple-600 rounded-lg p-6">
                <div className="bg-white/20 backdrop-blur-sm rounded-lg p-4">
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    {/* To Do Column */}
                    <div className="bg-white/30 rounded-lg p-4">
                      <h3 className="text-white font-semibold text-lg mb-4 text-center">{t("hero.kanban.todo")}</h3>
                      <div className="space-y-3">
                        <div className="bg-white/30 rounded p-3">
                          <p className="text-white text-sm text-center">{t("hero.kanban.tasks.uiDesign")}</p>
                        </div>
                        <div className="bg-white/30 rounded p-3">
                          <p className="text-white text-sm text-center">{t("hero.kanban.tasks.apiDesign")}</p>
                        </div>
                      </div>
                    </div>
                    
                    {/* In Progress Column */}
                    <div className="bg-white/30 rounded-lg p-4">
                      <h3 className="text-white font-semibold text-lg mb-4 text-center">{t("hero.kanban.inProgress")}</h3>
                      <div className="space-y-3">
                        <div className="bg-white/30 rounded p-3">
                          <p className="text-white text-sm text-center">{t("hero.kanban.tasks.loginFeature")}</p>
                        </div>
                        <div className="bg-white/30 rounded p-3">
                          <p className="text-white text-sm text-center">{t("hero.kanban.tasks.dashboard")}</p>
                        </div>
                      </div>
                    </div>
                    
                    {/* Done Column */}
                    <div className="bg-white/30 rounded-lg p-4">
                      <h3 className="text-white font-semibold text-lg mb-4 text-center">{t("hero.kanban.done")}</h3>
                      <div className="space-y-3">
                        <div className="bg-white/30 rounded p-3">
                          <p className="text-white text-sm text-center">{t("hero.kanban.tasks.projectSetup")}</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
} 