<#include "../security.ftl">
<#import "../pager.ftl" as p>


<#macro message page columns>

    <style>
        .card-columns {
            column-count: ${columns}!important;
        }
    </style>

    <div class="card-columns mt-3">
    <#list page.content as message>
        <#include "cardChoose.ftl">
    <#else>
        </div>
        <div class="card p-3 text-center">
            <blockquote class="blockquote mb-0 card-body">
                <p>Нет сообщений</p>
            </blockquote>
        </div>
    </#list>
    </div>

    <@p.pager url page />

</#macro>