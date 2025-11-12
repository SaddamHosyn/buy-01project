@echo off
cd /d "%~dp0"
echo Starting JSON Server on http://localhost:3000...
echo.
echo Available endpoints:
echo   GET    http://localhost:3000/products
echo   GET    http://localhost:3000/products/:id
echo   POST   http://localhost:3000/products
echo   PUT    http://localhost:3000/products/:id
echo   DELETE http://localhost:3000/products/:id
echo   GET    http://localhost:3000/users
echo   GET    http://localhost:3000/media
echo.
echo NOTE: Using basic JSON Server for data.
echo       Authentication handled by Angular (localStorage).
echo       Filtering done client-side (CSR approach).
echo.
call .\node_modules\.bin\json-server.cmd --watch db.json --port 3000 --host localhost
pause


