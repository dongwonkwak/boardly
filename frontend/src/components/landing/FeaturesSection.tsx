import { Users, Zap, Shield } from "lucide-react";
import { useTranslation } from "react-i18next";

export default function FeaturesSection() {
  const { t } = useTranslation("common");
  
  const features = [
    {
      id: "teamwork",
      icon: Users,
      title: t("features.teamwork.title"),
      description: t("features.teamwork.description"),
      gradient: "from-blue-600 to-purple-600",
    },
    {
      id: "fastManagement",
      icon: Zap,
      title: t("features.fastManagement.title"),
      description: t("features.fastManagement.description"),
      gradient: "from-emerald-500 to-teal-500",
    },
    {
      id: "secureData",
      icon: Shield,
      title: t("features.secureData.title"),
      description: t("features.secureData.description"),
      gradient: "from-amber-500 to-red-500",
    },
  ];
  return (
    <section className="py-20 bg-white" id="features">
      <div className="container mx-auto px-4">
        <div className="text-center max-w-4xl mx-auto mb-16">
          <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-gray-900 mb-6">
            {t("features.title")}
          </h2>
          <p className="text-lg md:text-xl text-gray-600 max-w-3xl mx-auto">
            {t("features.subtitle")}
          </p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
          {features.map((feature) => {
            const IconComponent = feature.icon;
            return (
              <div 
                key={feature.id}
                className="bg-white border border-gray-200 rounded-xl p-8 hover:shadow-lg transition-shadow duration-300"
              >
                <div className={`w-12 h-12 rounded-lg bg-gradient-to-r ${feature.gradient} flex items-center justify-center mb-6`}>
                  <IconComponent className="h-6 w-6 text-white" />
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-4">
                  {feature.title}
                </h3>
                <p className="text-gray-600 leading-relaxed whitespace-pre-line">
                  {feature.description}
                </p>
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
} 