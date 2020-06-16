<body>
<div>
    <h1>Sessions</h1>
    <#list report.getSessions() as session>
        <div>
            <h5>Session (${session.getSessionStart()?number_to_time}-${session.getSessionEnd()?number_to_time})</h5>
            <h5>Games</h5>
            <#list session.getItems() as item>
                <p>Mode: ${item.getMode()}</p>
                <p>Points: ${item.getPoints()}</p>
            </#list>
        </div>
    </#list>
</div>
<div>
    <h1>Daily points</h1>
    <#list report.getDailyPoints() as mode, pts>
        <p>
            Mode: ${mode}
            Points: ${pts}
        </p>
    </#list>
</div>
</body>