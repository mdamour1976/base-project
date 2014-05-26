<?xml version='1.0' encoding='UTF-8'?>
<% response.setCharacterEncoding("UTF-8"); %>
<% response.setContentType("text/xml"); %>

<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">

<%@page import="java.util.List"%>
<%@page import="org.damour.base.client.utils.StringUtils"%>
<%@page import="org.damour.base.server.hibernate.HibernateUtil"%>
<%@page import="org.damour.base.server.BaseSystem"%>
<%@page import="org.damour.base.client.objects.File"%>
<%@page import="org.hibernate.Session"%>

<%
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00"); 

		Session hibSession = HibernateUtil.getInstance().getSession();
		List results = HibernateUtil.getInstance().executeQuery(hibSession, "from File", true);
		for (int i=0;i<results.size();i++) { 
		  File file = (File)results.get(i);
		  String safeName = BaseSystem.patchURL(file.getName());
		  %>
			<url>
				<loc><%=BaseSystem.getBaseUrl(request)%>/view/id/<%=file.getId()%>/name/<%=safeName%>.html</loc>
				<lastmod><%=sdf.format(file.getLastModifiedDate())%></lastmod>
				<changefreq>weekly</changefreq>
				<priority>0.8</priority>
			</url>
<%		} %>

</urlset>
