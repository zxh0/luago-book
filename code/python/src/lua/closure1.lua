function newCounter ()
    local count = 0
    return function () -- 匿名函数
        count = count + 1
        return count
    end
end

c1 = newCounter()
print(c1()) --> 1
