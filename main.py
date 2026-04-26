import flet as ft
import socket
import threading
from http.server import BaseHTTPRequestHandler, HTTPServer
import time

# Global variables to control the server
target_url = "https://google.com"
server_port = 8080
server_running = False
httpd = None

class RedirectHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        # Redirect all requests to the target URL
        self.send_response(301)
        self.send_header('Location', target_url)
        self.end_headers()
    
    def log_message(self, format, *args):
        # Disable logging to console to keep background clean
        return

def run_server():
    global httpd, server_running
    server_address = ('', server_port)
    httpd = HTTPServer(server_address, RedirectHandler)
    server_running = True
    print(f"Server started on port {server_port}")
    httpd.serve_forever()

def get_local_ip():
    try:
        # Create a dummy socket to find the local IP
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"

def main(page: ft.Page):
    page.title = "Redirect Master"
    page.theme_mode = ft.ThemeMode.DARK
    page.window_width = 400
    page.window_height = 700
    page.padding = 20
    page.spacing = 20
    page.horizontal_alignment = ft.CrossAxisAlignment.CENTER
    page.vertical_alignment = ft.MainAxisAlignment.CENTER

    # Premium UI Styling
    primary_color = ft.colors.CYAN_400
    secondary_color = ft.colors.PURPLE_400
    
    # Custom Gradient Background Animation or Static Gradient
    bg_gradient = ft.LinearGradient(
        begin=ft.alignment.top_left,
        end=ft.alignment.bottom_right,
        colors=[ft.colors.GREY_900, ft.colors.BLUE_GREY_900]
    )
    
    # Text styles
    title_style = ft.TextStyle(size=28, weight="bold", color=ft.colors.WHITE)
    
    # Input field
    url_input = ft.TextField(
        label="أدخل الموقع المستهدف (مثل google.com)",
        placeholder="https://example.com",
        border_color=primary_color,
        focused_border_color=ft.colors.CYAN_200,
        text_align=ft.TextAlign.RIGHT,
        prefix_icon=ft.icons.LINK,
        border_radius=15,
        bgcolor=ft.colors.with_opacity(0.1, ft.colors.BLACK),
        on_submit=lambda e: start_redirect(e)
    )

    status_text = ft.Text("الخادم متوقف حالياً", color=ft.colors.RED_400, size=14, weight="w500")
    generated_link = ft.Text("", color=ft.colors.YELLOW_400, weight="bold", selectable=True, size=18)
    
    # Copy functionality
    def copy_link(e):
        if generated_link.value:
            page.set_clipboard(generated_link.value)
            page.show_snackbar(ft.SnackBar(
                content=ft.Text("تم النسخ!") ,
                bgcolor=ft.colors.GREEN_700
            ))

    copy_button = ft.IconButton(
        icon=ft.icons.COPY_ALL,
        icon_color=ft.colors.WHITE,
        on_click=copy_link,
        visible=False,
        tooltip="نسخ الرابط"
    )

    def start_redirect(e):
        global target_url, server_running, httpd
        input_val = url_input.value.strip()
        
        if not input_val:
            page.show_snackbar(ft.SnackBar(content=ft.Text("الرجاء إدخال رابط أولاً")))
            return

        # Prepare target URL
        if not (input_val.startswith("http://") or input_val.startswith("https://")):
            target_url = "https://" + input_val
        else:
            target_url = input_val
        
        # Start server if not running
        if not server_running:
            threading.Thread(target=run_server, daemon=True).start()
        
        # Update UI
        ip = get_local_ip()
        full_redirect_link = f"http://{ip}:{server_port}"
        
        generated_link.value = full_redirect_link
        status_text.value = f"الخادم يعمل ويقوم بالتوجيه إلى {target_url}"
        status_text.color = ft.colors.GREEN_400
        copy_button.visible = True
        
        # Visual feedback
        main_container.border = ft.border.all(2, ft.colors.CYAN_200)
        page.update()

    # Main Card Design
    main_container = ft.Container(
        content=ft.Column([
            ft.Container(
                content=ft.Icon(ft.icons.REPLY_ALL_ROUNDED, size=60, color=primary_color),
                padding=10,
                border_radius=50,
                bgcolor=ft.colors.with_opacity(0.1, ft.colors.CYAN_200),
            ),
            ft.Text("مُوجه المواقع الذكي", style=title_style),
            ft.Text("قم بتحويل هاتفك إلى نقطة إعادة توجيه", size=14, color=ft.colors.GREY_400),
            ft.Divider(height=20, color=ft.colors.with_opacity(0.1, ft.colors.WHITE)),
            
            url_input,
            
            ft.ElevatedButton(
                "تشغيل وإصدار الرابط",
                icon=ft.icons.BOLT,
                on_click=start_redirect,
                style=ft.ButtonStyle(
                    color=ft.colors.BLACK,
                    bgcolor={ft.MaterialState.DEFAULT: primary_color, ft.MaterialState.HOVERED: ft.colors.CYAN_200},
                    padding=20,
                    shape=ft.RoundedRectangleBorder(radius=15),
                ),
                width=float("inf"),
            ),
            
            ft.Container(
                content=ft.Column([
                    status_text,
                    ft.Row([
                        generated_link,
                        copy_button
                    ], alignment=ft.MainAxisAlignment.CENTER, spacing=10)
                ], horizontal_alignment=ft.CrossAxisAlignment.CENTER),
                padding=20,
                bgcolor=ft.colors.with_opacity(0.05, ft.colors.BLACK),
                border_radius=15,
            )
        ], horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=25),
        padding=40,
        bgcolor=ft.colors.with_opacity(0.1, ft.colors.WHITE),
        border_radius=35,
        blur=ft.Blur(15, 15),
        border=ft.border.all(1, ft.colors.with_opacity(0.1, ft.colors.WHITE)),
        shadow=ft.BoxShadow(
            spread_radius=1,
            blur_radius=20,
            color=ft.colors.with_opacity(0.2, ft.colors.BLACK),
            offset=ft.Offset(0, 10),
        )
    )

    page.add(
        ft.Stack([
            # Background decoration
            ft.Container(
                width=200, height=200,
                bgcolor=ft.colors.with_opacity(0.1, ft.colors.CYAN_700),
                border_radius=100,
                left=-100, top=-100,
                blur=ft.Blur(50, 50)
            ),
            ft.Container(
                width=200, height=200,
                bgcolor=ft.colors.with_opacity(0.1, ft.colors.PURPLE_700),
                border_radius=100,
                right=-100, bottom=-100,
                blur=ft.Blur(50, 50)
            ),
            main_container
        ], alignment=ft.alignment.center)
    )

ft.app(target=main)
