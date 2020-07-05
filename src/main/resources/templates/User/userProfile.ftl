<#import "/parts/bootstrap.ftl" as e>
<#import "/parts/message.ftl" as m>
<#include "/parts/security.ftl">

<@e.main true>

<div class="container mt-3">
    <div class="row">
        <div class="col-sm-12 col-md-12 col-lg-4">
            <div class="text-center">
                <figure class="figure">
                    <svg class="bd-placeholder-img figure-img img-fluid rounded-circle" width="150" height="150"
                         xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="xMidYMid slice" focusable="false"
                         role="img"><title></title>
                        <rect width="100%" height="100%" fill="#868e96"></rect>
                        <text x="50%" y="50%" fill="#dee2e6" dy=".3em"></text>
                    </svg>
                    <!--                <img src="" class="figure-img img-fluid rounded" alt="...">-->
                </figure>
            </div>
            <div class="list-group">
                <a class="list-group-item list-group-item-action text-center disabled">
                    ${userProfile.username}
                </a>
                <#if name == userProfile.username>
                    <a href="/${user.username}/profile/edit" class="list-group-item list-group-item-action">Редактировать
                        профиль</a>
                </#if>
                <a href="/${userProfile.username}/profile/messages" class="list-group-item list-group-item-action">Сообщения
                    пользователя</a>
            </div>
        </div>
        <div class="col-sm-12 col-md-12 col-lg-8">
            <#if messages??>
                <@m.message messages />
            <#elseif name == userProfile.username>
                <form method="post" action="/${user.username}/profile">
                    <div class="form-row">
                        <div class="form-group col-md-6">
                            <label for="username">Username</label>
                            <input type="text" name="username" class="form-control" value="${user.username}" id="username">
                        </div>
                        <div class="form-group col-md-6">
                            <label for="password">Password</label>
                            <input type="password" class="form-control" id="password" name="password" value="${user.password}">
                        </div>
                        <input type="hidden" value="${user.id}" name="userId">
                        <input type="hidden" value="${_csrf.token}" name="_csrf">
                    </div>
                    <button class="btn btn-primary btn-lg" type="submit">Save</button>
                </form>
            </#if>
        </div>
    </div>
</div>

</@e.main>