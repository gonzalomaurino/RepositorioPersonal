"""
Generación de reportes en PDF usando ReportLab.
Implementación según especificaciones del profesor.
"""
from reportlab.lib.pagesizes import letter
from reportlab.lib import colors
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, Image
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.lib.enums import TA_CENTER
import matplotlib.pyplot as plt
from io import BytesIO
from datetime import datetime
import os


class PDFReportService:
    """Servicio para generar reportes en PDF con ReportLab."""

    @staticmethod
    def generar_reporte_reservas_cliente(datos, ruta_salida="reportes/reservas_por_cliente.pdf"):
        """Genera un PDF con el reporte de reservas por cliente."""
        try:
            # Crear directorio si no existe
            os.makedirs(os.path.dirname(ruta_salida), exist_ok=True)
            
            # Crear documento PDF
            doc = SimpleDocTemplate(ruta_salida, pagesize=letter)
            elements = []
            styles = getSampleStyleSheet()
            
            # Estilo personalizado para el título
            title_style = ParagraphStyle(
                'CustomTitle',
                parent=styles['Heading1'],
                fontSize=18,
                textColor=colors.HexColor('#1f77b4'),
                spaceAfter=30,
                alignment=TA_CENTER
            )
            
            # Agregar título
            elements.append(Paragraph("Reporte de Reservas por Cliente", title_style))
            elements.append(Spacer(1, 12))
            
            # Agregar fecha de generación
            fecha_actual = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
            elements.append(Paragraph(f"<i>Fecha de generación: {fecha_actual}</i>", styles['Normal']))
            elements.append(Spacer(1, 20))
            
            # Crear tabla con datos
            if datos:
                # Preparar datos para la tabla
                tabla_datos = [["Cliente", "Total de Reservas"]]
                for cliente, total in datos:
                    tabla_datos.append([cliente, str(total)])
                
                # Crear tabla
                tabla = Table(tabla_datos, colWidths=[4 * inch, 2 * inch])
                tabla.setStyle(TableStyle([
                    ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#1f77b4')),
                    ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                    ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
                    ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
                    ('FONTSIZE', (0, 0), (-1, 0), 12),
                    ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
                    ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
                    ('GRID', (0, 0), (-1, -1), 1, colors.black),
                    ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.lightgrey])
                ]))
                elements.append(tabla)
                elements.append(Spacer(1, 30))
                
                # Generar y agregar gráfico
                elements.append(Paragraph("Distribución de Reservas", styles['Heading2']))
                elements.append(Spacer(1, 12))
                
                grafico_buffer = PDFReportService._crear_grafico_barras(datos, "Reservas por Cliente")
                if grafico_buffer:
                    img = Image(grafico_buffer, width=5*inch, height=3*inch)
                    elements.append(img)
            else:
                elements.append(Paragraph("No hay datos disponibles", styles['Normal']))
            
            # Construir PDF
            doc.build(elements)
            return ruta_salida
        except Exception as e:
            print(f"❌ Error generando PDF: {e}")
            raise

    @staticmethod
    def generar_reporte_reservas_cancha(datos, ruta_salida="reportes/reservas_por_cancha.pdf"):
        """Genera un PDF con el reporte de reservas por cancha."""
        try:
            # Crear directorio si no existe
            os.makedirs(os.path.dirname(ruta_salida), exist_ok=True)
            
            doc = SimpleDocTemplate(ruta_salida, pagesize=letter)
            elements = []
            styles = getSampleStyleSheet()
            
            title_style = ParagraphStyle(
                'CustomTitle',
                parent=styles['Heading1'],
                fontSize=18,
                textColor=colors.HexColor('#2ca02c'),
                spaceAfter=30,
                alignment=TA_CENTER
            )
            
            elements.append(Paragraph("Reporte de Reservas por Cancha", title_style))
            elements.append(Spacer(1, 12))
            
            fecha_actual = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
            elements.append(Paragraph(f"<i>Fecha de generación: {fecha_actual}</i>", styles['Normal']))
            elements.append(Spacer(1, 20))
            
            if datos:
                # Filtrar canchas con reservas
                datos_filtrados = [(cancha, total) for cancha, total in datos if total > 0]
                
                # Tabla
                tabla_datos = [["Cancha", "Total de Reservas"]]
                for cancha, total in datos:
                    tabla_datos.append([cancha, str(total)])
                
                tabla = Table(tabla_datos, colWidths=[4 * inch, 2 * inch])
                tabla.setStyle(TableStyle([
                    ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#2ca02c')),
                    ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                    ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
                    ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
                    ('FONTSIZE', (0, 0), (-1, 0), 12),
                    ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
                    ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
                    ('GRID', (0, 0), (-1, -1), 1, colors.black),
                    ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.lightgrey])
                ]))
                elements.append(tabla)
                elements.append(Spacer(1, 30))
                
                # Gráfico de pastel (solo canchas con reservas)
                if datos_filtrados:
                    elements.append(Paragraph("Distribución de Reservas", styles['Heading2']))
                    elements.append(Spacer(1, 12))
                    
                    grafico_buffer = PDFReportService._crear_grafico_pastel(datos_filtrados, "Reservas por Cancha")
                    if grafico_buffer:
                        img = Image(grafico_buffer, width=5*inch, height=3.5*inch)
                        elements.append(img)
            else:
                elements.append(Paragraph("No hay datos disponibles", styles['Normal']))
            
            doc.build(elements)
            return ruta_salida
        except Exception as e:
            print(f"❌ Error generando PDF: {e}")
            raise

    @staticmethod
    def generar_reporte_utilizacion_mensual(datos, ruta_salida="reportes/utilizacion_mensual.pdf"):
        """Genera un PDF con el reporte de utilización mensual."""
        try:
            os.makedirs(os.path.dirname(ruta_salida), exist_ok=True)
            
            doc = SimpleDocTemplate(ruta_salida, pagesize=letter)
            elements = []
            styles = getSampleStyleSheet()
            
            title_style = ParagraphStyle(
                'CustomTitle',
                parent=styles['Heading1'],
                fontSize=18,
                textColor=colors.HexColor('#ff7f0e'),
                spaceAfter=30,
                alignment=TA_CENTER
            )
            
            elements.append(Paragraph("Reporte de Utilización Mensual", title_style))
            elements.append(Spacer(1, 12))
            
            fecha_actual = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
            elements.append(Paragraph(f"<i>Fecha de generación: {fecha_actual}</i>", styles['Normal']))
            elements.append(Spacer(1, 20))
            
            if datos:
                # Tabla
                tabla_datos = [["Mes", "Total de Reservas"]]
                for mes, total in datos:
                    tabla_datos.append([mes, str(total)])
                
                tabla = Table(tabla_datos, colWidths=[3 * inch, 2 * inch])
                tabla.setStyle(TableStyle([
                    ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#ff7f0e')),
                    ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                    ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
                    ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
                    ('FONTSIZE', (0, 0), (-1, 0), 12),
                    ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
                    ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
                    ('GRID', (0, 0), (-1, -1), 1, colors.black),
                    ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.lightgrey])
                ]))
                elements.append(tabla)
                elements.append(Spacer(1, 30))
                
                # Gráfico de líneas
                elements.append(Paragraph("Tendencia de Reservas", styles['Heading2']))
                elements.append(Spacer(1, 12))
                
                grafico_buffer = PDFReportService._crear_grafico_lineas(datos, "Utilización Mensual")
                if grafico_buffer:
                    img = Image(grafico_buffer, width=5.5*inch, height=3*inch)
                    elements.append(img)
            else:
                elements.append(Paragraph("No hay datos disponibles", styles['Normal']))
            
            doc.build(elements)
            return ruta_salida
        except Exception as e:
            print(f"❌ Error generando PDF: {e}")
            raise

    @staticmethod
    def _crear_grafico_barras(datos, titulo):
        """Crea un gráfico de barras y lo retorna como BytesIO."""
        try:
            plt.figure(figsize=(6, 4))
            etiquetas = [d[0] for d in datos]
            valores = [d[1] for d in datos]
            
            plt.bar(range(len(etiquetas)), valores, color='#1f77b4', alpha=0.8, edgecolor='black')
            plt.xticks(range(len(etiquetas)), etiquetas, rotation=45, ha='right')
            plt.ylabel('Cantidad de Reservas', fontsize=10, fontweight='bold')
            plt.title(titulo, fontsize=12, fontweight='bold')
            plt.grid(axis='y', alpha=0.3)
            plt.tight_layout()
            
            buffer = BytesIO()
            plt.savefig(buffer, format='png', dpi=150, bbox_inches='tight')
            buffer.seek(0)
            plt.close()
            
            return buffer
        except Exception as e:
            print(f"❌ Error creando gráfico de barras: {e}")
            return None

    @staticmethod
    def _crear_grafico_pastel(datos, titulo):
        """Crea un gráfico de pastel y lo retorna como BytesIO."""
        try:
            plt.figure(figsize=(6, 5))
            etiquetas = [d[0] for d in datos]
            valores = [d[1] for d in datos]
            
            colors_list = plt.cm.Set3(range(len(etiquetas)))
            plt.pie(valores, labels=etiquetas, autopct='%1.1f%%', colors=colors_list,
                   startangle=90, textprops={'fontsize': 9, 'weight': 'bold'})
            plt.title(titulo, fontsize=12, fontweight='bold')
            plt.axis('equal')
            plt.tight_layout()
            
            buffer = BytesIO()
            plt.savefig(buffer, format='png', dpi=150, bbox_inches='tight')
            buffer.seek(0)
            plt.close()
            
            return buffer
        except Exception as e:
            print(f"❌ Error creando gráfico de pastel: {e}")
            return None

    @staticmethod
    def _crear_grafico_lineas(datos, titulo):
        """Crea un gráfico de líneas y lo retorna como BytesIO."""
        try:
            plt.figure(figsize=(7, 4))
            etiquetas = [d[0] for d in datos]
            valores = [d[1] for d in datos]
            
            plt.plot(range(len(etiquetas)), valores, marker='o', linewidth=2,
                    markersize=8, color='#ff7f0e', label='Reservas')
            plt.fill_between(range(len(etiquetas)), valores, alpha=0.3, color='#ff7f0e')
            plt.xticks(range(len(etiquetas)), etiquetas, rotation=45, ha='right')
            plt.ylabel('Cantidad de Reservas', fontsize=10, fontweight='bold')
            plt.title(titulo, fontsize=12, fontweight='bold')
            plt.grid(True, alpha=0.3)
            plt.legend(fontsize=9)
            plt.tight_layout()
            
            buffer = BytesIO()
            plt.savefig(buffer, format='png', dpi=150, bbox_inches='tight')
            buffer.seek(0)
            plt.close()
            
            return buffer
        except Exception as e:
            print(f"❌ Error creando gráfico de líneas: {e}")
            return None

    @staticmethod
    def generar_reporte_costos(estado, ingresos_cancha, pagos_metodo, presupuesto, clientes,
                               ruta_salida="reportes/reporte_costos.pdf"):
        """Genera un PDF con el reporte completo de costos y presupuestos."""
        try:
            os.makedirs(os.path.dirname(ruta_salida), exist_ok=True)
            
            doc = SimpleDocTemplate(ruta_salida, pagesize=letter)
            elements = []
            styles = getSampleStyleSheet()
            
            title_style = ParagraphStyle(
                'CustomTitle',
                parent=styles['Heading1'],
                fontSize=18,
                textColor=colors.HexColor('#d62728'),
                spaceAfter=30,
                alignment=TA_CENTER
            )
            
            # Título
            elements.append(Paragraph("Reporte de Costos y Presupuestos", title_style))
            elements.append(Spacer(1, 12))
            
            fecha_actual = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
            elements.append(Paragraph(f"<i>Fecha de generación: {fecha_actual}</i>", styles['Normal']))
            elements.append(Spacer(1, 20))
            
            # Estado general
            if estado:
                elements.append(Paragraph("📊 Estado General de Pagos e Ingresos", styles['Heading2']))
                elements.append(Spacer(1, 12))
                
                estado_datos = [
                    ["Concepto", "Monto ($)"],
                    ["Ingresos Confirmados", f"${estado.get('ingresos_confirmados', 0):,.2f}"],
                    ["Pagos Realizados", f"${estado.get('pagos_realizados', 0):,.2f}"],
                    ["Pendientes", f"${estado.get('pendientes', 0):,.2f}"],
                    ["Canceladas", f"${estado.get('canceladas', 0):,.2f}"],
                    ["Balance", f"${estado.get('balance', 0):,.2f}"],
                ]
                
                tabla_estado = Table(estado_datos, colWidths=[3.5*inch, 2*inch])
                tabla_estado.setStyle(TableStyle([
                    ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#d62728')),
                    ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                    ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
                    ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
                    ('FONTSIZE', (0, 0), (-1, 0), 11),
                    ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
                    ('BACKGROUND', (0, 1), (-1, -1), colors.lightgrey),
                    ('GRID', (0, 0), (-1, -1), 1, colors.black),
                    ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.lightgrey])
                ]))
                elements.append(tabla_estado)
                elements.append(Spacer(1, 20))
            
            # Ingresos por cancha
            if ingresos_cancha:
                elements.append(Paragraph("💵 Ingresos por Cancha", styles['Heading2']))
                elements.append(Spacer(1, 12))
                
                tabla_ingresos_datos = [["Cancha", "Reservas", "Ingresos ($)"]]
                for cancha, ingresos, reservas in ingresos_cancha:
                    tabla_ingresos_datos.append([
                        cancha,
                        str(reservas),
                        f"${ingresos if ingresos else 0:,.2f}"
                    ])
                
                tabla_ingresos = Table(tabla_ingresos_datos, colWidths=[3*inch, 1.5*inch, 2*inch])
                tabla_ingresos.setStyle(TableStyle([
                    ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#2ca02c')),
                    ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                    ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
                    ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
                    ('FONTSIZE', (0, 0), (-1, 0), 10),
                    ('BOTTOMPADDING', (0, 0), (-1, 0), 10),
                    ('GRID', (0, 0), (-1, -1), 1, colors.black),
                    ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.lightgrey])
                ]))
                elements.append(tabla_ingresos)
                elements.append(Spacer(1, 20))
            
            # Pagos por método
            if pagos_metodo:
                elements.append(Paragraph("💳 Pagos Realizados por Método", styles['Heading2']))
                elements.append(Spacer(1, 12))
                
                tabla_pagos_datos = [["Método", "Cantidad", "Total ($)"]]
                for metodo, cantidad, total in pagos_metodo:
                    tabla_pagos_datos.append([
                        metodo.capitalize(),
                        str(cantidad),
                        f"${total if total else 0:,.2f}"
                    ])
                
                tabla_pagos = Table(tabla_pagos_datos, colWidths=[2.5*inch, 1.5*inch, 2*inch])
                tabla_pagos.setStyle(TableStyle([
                    ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#1f77b4')),
                    ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                    ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
                    ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
                    ('FONTSIZE', (0, 0), (-1, 0), 10),
                    ('BOTTOMPADDING', (0, 0), (-1, 0), 10),
                    ('GRID', (0, 0), (-1, -1), 1, colors.black),
                    ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.lightgrey])
                ]))
                elements.append(tabla_pagos)
                elements.append(Spacer(1, 20))
            
            # Presupuesto mensual
            if presupuesto:
                elements.append(Paragraph("📅 Presupuesto vs Cobrado Mensualmente", styles['Heading2']))
                elements.append(Spacer(1, 12))
                
                tabla_presupuesto_datos = [["Mes", "Reservas", "Presupuesto ($)", "Cobrado ($)", "Diferencia ($)"]]
                for mes, reservas, presup, pagos in presupuesto:
                    diferencia = presup - pagos if presup and pagos else 0
                    tabla_presupuesto_datos.append([
                        mes,
                        str(reservas),
                        f"${presup if presup else 0:,.2f}",
                        f"${pagos if pagos else 0:,.2f}",
                        f"${diferencia:,.2f}"
                    ])
                
                tabla_presupuesto = Table(tabla_presupuesto_datos, colWidths=[1.5*inch, 1*inch, 1.5*inch, 1.5*inch, 1.5*inch])
                tabla_presupuesto.setStyle(TableStyle([
                    ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#ff7f0e')),
                    ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                    ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
                    ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
                    ('FONTSIZE', (0, 0), (-1, 0), 9),
                    ('BOTTOMPADDING', (0, 0), (-1, 0), 10),
                    ('GRID', (0, 0), (-1, -1), 1, colors.black),
                    ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.lightgrey])
                ]))
                elements.append(tabla_presupuesto)
                elements.append(Spacer(1, 20))
            
            # Top clientes
            if clientes:
                elements.append(Paragraph("👥 Top Clientes por Inversión", styles['Heading2']))
                elements.append(Spacer(1, 12))
                
                tabla_clientes_datos = [["Cliente", "Reservas", "Invertido ($)", "Pagado ($)"]]
                for cliente, reservas, invertido, pagado in clientes[:10]:
                    tabla_clientes_datos.append([
                        cliente,
                        str(reservas),
                        f"${invertido if invertido else 0:,.2f}",
                        f"${pagado if pagado else 0:,.2f}"
                    ])
                
                tabla_clientes = Table(tabla_clientes_datos, colWidths=[2.5*inch, 1*inch, 1.5*inch, 1.5*inch])
                tabla_clientes.setStyle(TableStyle([
                    ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#9467bd')),
                    ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                    ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
                    ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
                    ('FONTSIZE', (0, 0), (-1, 0), 9),
                    ('BOTTOMPADDING', (0, 0), (-1, 0), 10),
                    ('GRID', (0, 0), (-1, -1), 1, colors.black),
                    ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.lightgrey])
                ]))
                elements.append(tabla_clientes)
            
            doc.build(elements)
            return ruta_salida
        except Exception as e:
            print(f"❌ Error generando PDF de costos: {e}")
            raise
