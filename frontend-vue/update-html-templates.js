// HTML 템플릿 파일 업데이트 스크립트
// 빌드된 assets 파일명을 HTML 템플릿에 반영

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// 빌드된 assets 디렉토리
const assetsDir = path.join(__dirname, '../src/main/resources/static/vue-dist/assets');
const templatesDir = path.join(__dirname, '../src/main/resources/templates');
const staticDir = path.join(__dirname, '../src/main/resources/static/vue-dist');

// 각 페이지별 매핑
const pageMapping = {
  'dashboard': 'dashboard/vue-dashboard.html',
  'monitor': 'monitor/vue-monitor.html',
  'configure': 'configure/vue-configure.html',
  'results': 'results/vue-results.html'
};

// static 디렉토리의 HTML 파일들
const staticHtmlFiles = {
  'results': 'results.html',
  'monitor': 'monitor.html',
  'configure': 'configure.html',
  'dashboard': 'dashboard.html'
};

// assets 디렉토리에서 CSS 파일 찾기
const cssFiles = fs.readdirSync(assetsDir).filter(file => file.endsWith('.css'));

// 각 페이지별 CSS 파일 찾기
const pageCssMap = {};
cssFiles.forEach(file => {
  if (file.startsWith('main-')) {
    pageCssMap.main = file;
  } else {
    Object.keys(pageMapping).forEach(page => {
      if (file.startsWith(page + '-')) {
        pageCssMap[page] = file;
      }
    });
  }
});

console.log('Found CSS files:', pageCssMap);

// 각 HTML 템플릿 업데이트
Object.entries(pageMapping).forEach(([page, templatePath]) => {
  const fullPath = path.join(templatesDir, templatePath);
  
  if (fs.existsSync(fullPath)) {
    let content = fs.readFileSync(fullPath, 'utf8');
    
    // main CSS 업데이트
    if (pageCssMap.main) {
      content = content.replace(
        /assets\/main-[A-Za-z0-9_-]+\.css/g,
        `assets/${pageCssMap.main}`
      );
    }
    
    // 페이지별 CSS 업데이트
    if (pageCssMap[page]) {
      const pattern = new RegExp(`assets/${page}-[A-Za-z0-9_-]+\\.css`, 'g');
      content = content.replace(pattern, `assets/${pageCssMap[page]}`);
    }
    
    fs.writeFileSync(fullPath, content);
    console.log(`Updated ${templatePath}`);
  }
});

// static 디렉토리의 HTML 파일들도 업데이트
Object.entries(staticHtmlFiles).forEach(([page, htmlFile]) => {
  const fullPath = path.join(staticDir, htmlFile);
  
  if (fs.existsSync(fullPath)) {
    let content = fs.readFileSync(fullPath, 'utf8');
    
    // </body> 태그 앞에 CSS와 JS 링크 추가
    const hasAssets = content.includes('assets/main-');
    
    if (!hasAssets) {
      let insertContent = '';
      
      // CSS 링크 추가
      if (pageCssMap.main) {
        insertContent += `    <link rel="stylesheet" href="assets/${pageCssMap.main}">\n`;
      }
      if (pageCssMap[page]) {
        insertContent += `    <link rel="stylesheet" href="assets/${pageCssMap[page]}">\n`;
      }
      
      // JS 링크 추가 (results.js 등)
      insertContent += `    \n    <script type="module" src="${page}.js"></script>\n`;
      
      // </body> 태그 앞에 삽입
      content = content.replace('</body>', insertContent + '</body>');
      
      fs.writeFileSync(fullPath, content);
      console.log(`Updated static/${htmlFile}`);
    }
  }
});

console.log('HTML templates updated successfully!');