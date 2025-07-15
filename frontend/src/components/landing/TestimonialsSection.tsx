import { Star } from "lucide-react";
import { useTranslation } from "react-i18next";

export default function TestimonialsSection() {
  const { t } = useTranslation("common");
  
  const testimonials = [
    {
      name: t("testimonials.users.developer.name"),
      role: t("testimonials.users.developer.role"),
      company: t("testimonials.users.developer.company"),
      content: t("testimonials.users.developer.content"),
      avatar: t("testimonials.users.developer.avatar"),
      rating: 5,
    },
    {
      name: t("testimonials.users.manager.name"),
      role: t("testimonials.users.manager.role"),
      company: t("testimonials.users.manager.company"),
      content: t("testimonials.users.manager.content"),
      avatar: t("testimonials.users.manager.avatar"),
      rating: 5,
    },
    {
      name: t("testimonials.users.student.name"),
      role: t("testimonials.users.student.role"),
      company: t("testimonials.users.student.company"),
      content: t("testimonials.users.student.content"),
      avatar: t("testimonials.users.student.avatar"),
      rating: 5,
    },
  ];
  return (
    <section className="py-20 bg-gradient-to-br from-slate-50 via-blue-50 to-slate-50" id="testimonials">
      <div className="container mx-auto px-4">
        <div className="text-center max-w-4xl mx-auto mb-16">
          <h2 className="text-3xl md:text-4xl lg:text-5xl font-bold text-gray-900 mb-6">
            {t("testimonials.title")}
          </h2>
          <p className="text-lg md:text-xl text-gray-600">
            {t("testimonials.subtitle")}
          </p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
          {testimonials.map((testimonial) => (
            <div 
              key={testimonial.name}
              className="bg-white border border-gray-200 rounded-xl p-8 hover:shadow-lg transition-shadow duration-300"
            >
              {/* Rating */}
              <div className="flex items-center gap-1 mb-6">
                {[...Array(testimonial.rating)].map((_, i) => (
                  <Star key={`${testimonial.name}-star-${i}`} className="h-5 w-5 fill-yellow-400 text-yellow-400" />
                ))}
              </div>
              
              {/* Testimonial Content */}
              <blockquote className="text-gray-600 italic mb-8 leading-relaxed whitespace-pre-line">
                "{testimonial.content}"
              </blockquote>
              
              {/* User Info */}
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-full bg-gradient-to-r from-blue-600 to-purple-600 flex items-center justify-center">
                  <span className="text-white font-bold">
                    {testimonial.avatar}
                  </span>
                </div>
                <div>
                  <div className="font-semibold text-gray-900">
                    {testimonial.name}
                  </div>
                  <div className="text-sm text-gray-600">
                    {testimonial.role} • {testimonial.company}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
} 