/*
 Highstock JS v4.2.1 (2015-12-21)
 Client side exporting module

 (c) 2015 Torstein Honsi / Oystein Moseng

 License: www.highcharts.com/license
*/
(function(c){typeof module==="object"&&module.exports?module.exports=c:c(Highcharts)})(function(c){function z(c,f){var h=l.getElementsByTagName("head")[0],b=l.createElement("script");b.type="text/javascript";b.src=c;b.onload=f;h.appendChild(b)}var f=c.win,i=f.navigator,l=f.document;c.CanVGRenderer={};c.Chart.prototype.exportChartLocal=function(A,B){var h=this,b=c.merge(h.options.exporting,A),s=i.userAgent.indexOf("WebKit")>-1&&i.userAgent.indexOf("Chrome")<0,n=b.scale||2,p,t=f.URL||f.webkitURL||f,
k,u=0,q,o,v,d=function(){if(b.fallbackToExportServer===!1)throw"Fallback to export server disabled";h.exportChart(b)},w=function(a,g,e,c,m,b,d){var j=new f.Image;if(!s)j.crossOrigin="Anonymous";j.onload=function(){var b=l.createElement("canvas"),f=b.getContext&&b.getContext("2d"),h;if(f){b.height=j.height*n;b.width=j.width*n;f.drawImage(j,0,0,b.width,b.height);try{h=b.toDataURL(),e(h,g)}catch(i){if(i.name==="SecurityError"||i.name==="SECURITY_ERR"||i.message==="SecurityError")c(a,g);else throw i;
}}else m(a,g);d&&d(a,g)};j.onerror=function(){b(a,g);d&&d(a,g)};j.src=a},x=function(a){try{if(!s&&i.userAgent.toLowerCase().indexOf("firefox")<0)return t.createObjectURL(new f.Blob([a],{type:"image/svg+xml;charset-utf-16"}))}catch(b){}return"data:image/svg+xml;charset=UTF-8,"+encodeURIComponent(a)},r=function(a,c){var e=l.createElement("a"),d=(b.filename||"chart")+"."+c,m;if(i.msSaveOrOpenBlob)i.msSaveOrOpenBlob(a,d);else if(e.download!==void 0)e.href=a,e.download=d,e.target="_blank",l.body.appendChild(e),
e.click(),l.body.removeChild(e);else try{if(m=f.open(a,"chart"),m===void 0||m===null)throw 1;}catch(h){f.location.href=a}},y=function(){var a,g,e=h.sanitizeSVG(p.innerHTML);if(b&&b.type==="image/svg+xml")try{i.msSaveOrOpenBlob?(g=new MSBlobBuilder,g.append(e),a=g.getBlob("image/svg+xml")):a=x(e),r(a,"svg")}catch(k){d()}else a=x(e),w(a,{},function(a){try{r(a,"png")}catch(b){d()}},function(){var a=l.createElement("canvas"),b=a.getContext("2d"),g=e.match(/^<svg[^>]*width\s*=\s*\"?(\d+)\"?[^>]*>/)[1]*
n,j=e.match(/^<svg[^>]*height\s*=\s*\"?(\d+)\"?[^>]*>/)[1]*n,k=function(){b.drawSvg(e,0,0,g,j);try{r(i.msSaveOrOpenBlob?a.msToBlob():a.toDataURL("image/png"),"png")}catch(c){d()}};a.width=g;a.height=j;f.canvg?k():(h.showLoading(),z(c.getOptions().global.canvasToolsURL,function(){h.hideLoading();k()}))},d,d,function(){try{t.revokeObjectURL(a)}catch(b){}})},C=function(a,b){++u;b.imageElement.setAttributeNS("http://www.w3.org/1999/xlink","href",a);u===k.length&&y()};c.wrap(c.Chart.prototype,"getChartHTML",
function(a){p=this.container.cloneNode(!0);return a.apply(this,Array.prototype.slice.call(arguments,1))});h.getSVGForExport(b,B);k=p.getElementsByTagName("image");try{k.length||y();for(o=0,v=k.length;o<v;++o)q=k[o],w(q.getAttributeNS("http://www.w3.org/1999/xlink","href"),{imageElement:q},C,d,d,d)}catch(D){d()}};c.getOptions().exporting.buttons.contextButton.menuItems=[{textKey:"printChart",onclick:function(){this.print()}},{separator:!0},{textKey:"downloadPNG",onclick:function(){this.exportChartLocal()}},
{textKey:"downloadSVG",onclick:function(){this.exportChartLocal({type:"image/svg+xml"})}}]});
