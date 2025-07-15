import { Button } from "@/components/ui/button";
import { Check } from "lucide-react";
import { useTranslation } from "react-i18next";

interface PricingSectionProps {
  onLogin?: () => void;
}

export default function PricingSection({ onLogin }: PricingSectionProps) {
  const { t } = useTranslation("common");
  
  const pricingPlans = [
    {
      id: "personal",
      name: t("pricing.plans.personal.name"),
      price: t("pricing.plans.personal.price"),
      period: "",
      description: t("pricing.plans.personal.description"),
      features: t("pricing.plans.personal.features", { returnObjects: true }) as string[],
      buttonText: t("pricing.plans.personal.button"),
      buttonVariant: "outline" as const,
      popular: false,
    },
    {
      id: "team",
      name: t("pricing.plans.team.name"),
      price: t("pricing.plans.team.price"),
      period: t("pricing.plans.team.period"),
      description: t("pricing.plans.team.description"),
      features: t("pricing.plans.team.features", { returnObjects: true }) as string[],
      buttonText: t("pricing.plans.team.button"),
      buttonVariant: "default" as const,
      popular: true,
    },
    {
      id: "enterprise",
      name: t("pricing.plans.enterprise.name"),
      price: t("pricing.plans.enterprise.price"),
      period: "",
      description: t("pricing.plans.enterprise.description"),
      features: t("pricing.plans.enterprise.features", { returnObjects: true }) as string[],
      buttonText: t("pricing.plans.enterprise.button"),
      buttonVariant: "outline" as const,
      popular: false,
    },
  ];
  return (
    <section className="py-20 bg-white" id="pricing">
      <div className="container mx-auto px-4">
        <div className="text-center max-w-4xl mx-auto mb-16">
          <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-gray-900 mb-6">
            {t("pricing.title")}
          </h2>
          <p className="text-lg md:text-xl text-gray-600">
            {t("pricing.subtitle")}
          </p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
          {pricingPlans.map((plan) => (
            <div 
              key={plan.id}
              className={`relative bg-white rounded-xl p-8 transition-all duration-300 ${
                plan.popular 
                  ? 'border-2 border-blue-600 shadow-xl scale-105' 
                  : 'border border-gray-200 hover:shadow-lg'
              }`}
            >
              {/* Popular Badge */}
              {plan.popular && (
                <div className="absolute -top-4 left-1/2 transform -translate-x-1/2">
                  <div className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-6 py-2 rounded-full text-sm font-medium">
                    {t("pricing.popularBadge")}
                  </div>
                </div>
              )}
              
              {/* Plan Header */}
              <div className="text-center mb-8">
                <h3 className="text-2xl font-bold text-gray-900 mb-4">
                  {plan.name}
                </h3>
                <div className="mb-4">
                  <span className="text-4xl md:text-5xl font-bold text-gray-900">
                    {plan.price}
                  </span>
                  {plan.period && (
                    <span className="text-gray-600 ml-2">
                      {plan.period}
                    </span>
                  )}
                </div>
                <p className="text-gray-600 whitespace-pre-line">
                  {plan.description}
                </p>
              </div>
              
              {/* Features List */}
              <div className="mb-8">
                <ul className="space-y-4">
                  {plan.features.map((feature) => (
                    <li key={feature} className="flex items-center gap-3">
                      <Check className="h-5 w-5 text-green-500 flex-shrink-0" />
                      <span className="text-gray-700">{feature}</span>
                    </li>
                  ))}
                </ul>
              </div>
              
              {/* CTA Button */}
              <Button 
                variant={plan.buttonVariant}
                className={`w-full py-3 font-medium ${
                  plan.buttonVariant === 'default' 
                    ? 'bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white' 
                    : ''
                }`}
                onClick={onLogin}
              >
                {plan.buttonText}
              </Button>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
} 