import matplotlib.pyplot as plt
import matplotlib
import io
import base64
from typing import List, Tuple

# Usar backend no interactivo
matplotlib.use('Agg')

class GraficosService:
    """Servicio para generar gráficos estáticos convertidos a base64."""

    @staticmethod
    def generar_grafico_barras(datos: List[Tuple], titulo: str, xlabel: str, ylabel: str) -> str:
        """Genera un gráfico de barras y retorna como base64."""
        try:
            fig, ax = plt.subplots(figsize=(8, 5))
            
            etiquetas = [d[0] for d in datos]
            valores = [d[1] for d in datos]
            
            ax.bar(range(len(etiquetas)), valores, color='#1f77b4', alpha=0.8, edgecolor='black')
            ax.set_xticks(range(len(etiquetas)))
            ax.set_xticklabels(etiquetas, rotation=45, ha='right')
            ax.set_ylabel(ylabel, fontsize=12, fontweight='bold')
            ax.set_xlabel(xlabel, fontsize=12, fontweight='bold')
            ax.set_title(titulo, fontsize=14, fontweight='bold')
            ax.grid(axis='y', alpha=0.3)
            
            plt.tight_layout()
            
            # Convertir a base64
            buffer = io.BytesIO()
            plt.savefig(buffer, format='png', dpi=100, bbox_inches='tight')
            buffer.seek(0)
            img_base64 = base64.b64encode(buffer.read()).decode()
            plt.close(fig)
            
            return img_base64
        except Exception as e:
            print(f"❌ Error al generar gráfico de barras: {e}")
            return None

    @staticmethod
    def generar_grafico_pastel(datos: List[Tuple], titulo: str) -> str:
        """Genera un gráfico de pastel (pie chart) y retorna como base64."""
        try:
            fig, ax = plt.subplots(figsize=(8, 5))
            
            # Filtrar datos con valores > 0 para no mostrar 0%
            datos_filtrados = [(etiqueta, valor) for etiqueta, valor in datos if valor > 0]
            
            if not datos_filtrados:
                # Si no hay datos, mostrar mensaje
                ax.text(0.5, 0.5, 'No hay datos disponibles', 
                       ha='center', va='center', fontsize=14, transform=ax.transAxes)
                ax.axis('off')
            else:
                etiquetas = [d[0] for d in datos_filtrados]
                valores = [d[1] for d in datos_filtrados]
                
                # Usar colores variados
                colors = plt.cm.Set3(range(len(etiquetas)))
                
                # Mostrar porcentajes (sin mostrar 0% ya que están filtrados)
                ax.pie(valores, labels=etiquetas, autopct='%1.1f%%', colors=colors,
                       startangle=90, textprops={'fontsize': 10, 'weight': 'bold'})
                ax.set_title(titulo, fontsize=14, fontweight='bold')
            
            plt.tight_layout()
            
            # Convertir a base64
            buffer = io.BytesIO()
            plt.savefig(buffer, format='png', dpi=100, bbox_inches='tight')
            buffer.seek(0)
            img_base64 = base64.b64encode(buffer.read()).decode()
            plt.close(fig)
            
            return img_base64
        except Exception as e:
            print(f"❌ Error al generar gráfico de pastel: {e}")
            return None

    @staticmethod
    def generar_grafico_lineas(datos: List[Tuple], titulo: str, xlabel: str, ylabel: str) -> str:
        """Genera un gráfico de líneas y retorna como base64."""
        try:
            fig, ax = plt.subplots(figsize=(8, 5))
            
            etiquetas = [d[0] for d in datos]
            valores = [d[1] for d in datos]
            
            ax.plot(range(len(etiquetas)), valores, marker='o', linewidth=2, 
                   markersize=8, color='#ff7f0e', label='Reservas')
            ax.fill_between(range(len(etiquetas)), valores, alpha=0.3, color='#ff7f0e')
            
            ax.set_xticks(range(len(etiquetas)))
            ax.set_xticklabels(etiquetas, rotation=45, ha='right')
            ax.set_ylabel(ylabel, fontsize=12, fontweight='bold')
            ax.set_xlabel(xlabel, fontsize=12, fontweight='bold')
            ax.set_title(titulo, fontsize=14, fontweight='bold')
            ax.grid(True, alpha=0.3)
            ax.legend(fontsize=10)
            
            plt.tight_layout()
            
            # Convertir a base64
            buffer = io.BytesIO()
            plt.savefig(buffer, format='png', dpi=100, bbox_inches='tight')
            buffer.seek(0)
            img_base64 = base64.b64encode(buffer.read()).decode()
            plt.close(fig)
            
            return img_base64
        except Exception as e:
            print(f"❌ Error al generar gráfico de líneas: {e}")
            return None
